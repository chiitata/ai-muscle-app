import { NextRequest, NextResponse } from "next/server";
import Stripe from "stripe";
import { createClient } from "@supabase/supabase-js";
import { PLANS, PlanKey, ONE_TIME_PLANS, OneTimeKey } from "@/lib/plans";

// このルートはリクエスト毎に実行する（ビルド時の静的解析で Stripe を初期化しない）。
export const dynamic = "force-dynamic";

export async function POST(req: NextRequest) {
  try {
    // STRIPE_SECRET_KEY が未投入（キー配線前）の間は決済セッションを作れない。
    // モジュール評価ではなくリクエスト時に初期化し、未設定ならクリーンに返す。
    const stripeKey = process.env.STRIPE_SECRET_KEY;
    if (!stripeKey) {
      return NextResponse.json(
        { error: "決済は現在準備中です。しばらくお待ちください。" },
        { status: 503 },
      );
    }
    const stripe = new Stripe(stripeKey);

    // ユーザー検証用 Supabase クライアント（anon キーで getUser(jwt) を実行）。
    const supabase = createClient(
      process.env.NEXT_PUBLIC_SUPABASE_URL!,
      process.env.NEXT_PUBLIC_SUPABASE_ANON_KEY!,
    );

    const { plan, kind, accessToken, source } = (await req.json()) as {
      plan: PlanKey | OneTimeKey;
      kind?: "subscription" | "onetime";
      accessToken?: string;
      source?: "mobile_app";
    };

    // 認証: クライアントの Supabase セッション JWT を検証して本人の user id を得る。
    // これを client_reference_id に使う＝URL改ざんで他人のプランを有効化させない。
    const token = accessToken ?? req.headers.get("authorization")?.replace(/^Bearer /, "");
    if (!token) {
      return NextResponse.json({ error: "ログインが必要です。" }, { status: 401 });
    }
    const { data: userData, error: userErr } = await supabase.auth.getUser(token);
    if (userErr || !userData.user) {
      return NextResponse.json({ error: "セッションが無効です。再ログインしてください。" }, { status: 401 });
    }
    const userId = userData.user.id;

    const origin = req.headers.get("origin") ?? req.nextUrl.origin;
    const isOnetime = kind === "onetime";
    const isMobileApp = source === "mobile_app";
    // Universal Link でアプリに即時復帰させる URL (W1 iOS と合意済みのコントラクト)。
    const UNIVERSAL_SUCCESS_URL =
      "https://studyai-web-gamma.vercel.app/app/return?session_id={CHECKOUT_SESSION_ID}";
    const successUrl = isMobileApp ? UNIVERSAL_SUCCESS_URL : `${origin}/success`;
    const cancelUrl = isMobileApp ? `${origin}/app/cancel` : `${origin}/cancel`;

    let session;
    if (isOnetime) {
      const p = ONE_TIME_PLANS[plan as OneTimeKey];
      if (!p) return NextResponse.json({ error: "プランが不正です。" }, { status: 400 });

      session = await stripe.checkout.sessions.create({
        mode: "payment",
        client_reference_id: userId,
        payment_method_types: ["card", "paypay"] as Stripe.Checkout.SessionCreateParams["payment_method_types"],
        line_items: [
          {
            quantity: 1,
            price_data: {
              currency: "jpy",
              unit_amount: p.amount,
              product_data: { name: p.label },
            },
          },
        ],
        // webhookが grant_days と supabase_user_id を読む（mac3 webhook と共有コントラクト）。
        payment_intent_data: {
          metadata: { supabase_user_id: userId, grant_days: String(p.days) },
        },
        metadata: { supabase_user_id: userId, grant_days: String(p.days), kind: "onetime" },
        success_url: successUrl,
        cancel_url: cancelUrl,
        customer_email: userData.user.email ?? undefined,
      });
    } else {
      const p = PLANS[plan as PlanKey];
      if (!p) return NextResponse.json({ error: "プランが不正です。" }, { status: 400 });

      session = await stripe.checkout.sessions.create({
        mode: "subscription",
        client_reference_id: userId,
        // webhook(apply_stripe_subscription) がここから Supabase user を解決する。
        subscription_data: { metadata: { supabase_user_id: userId } },
        ...(isMobileApp ? { origin_context: "mobile_app" } as Record<string, unknown> : {}),
        line_items: [
          {
            quantity: 1,
            price_data: {
              currency: "jpy",
              unit_amount: p.amount,
              recurring: { interval: p.interval },
              product_data: { name: p.label },
            },
          },
        ],
        success_url: successUrl,
        cancel_url: cancelUrl,
        // 同一ユーザーの重複顧客作成を避けるため email を引き継ぐ。
        customer_email: userData.user.email ?? undefined,
      });
    }

    return NextResponse.json({ url: session.url });
  } catch (e) {
    console.error("checkout error", e);
    return NextResponse.json({ error: "決済セッションの作成に失敗しました。" }, { status: 500 });
  }
}
