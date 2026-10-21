"use client";

import { useEffect, useState } from "react";
import type { Session } from "@supabase/supabase-js";
import { supabase } from "@/lib/supabaseClient";
import { PLANS, PlanKey, ONE_TIME_PLANS, OneTimeKey } from "@/lib/plans";

const card: React.CSSProperties = {
  background: "#fff",
  borderRadius: 16,
  padding: 24,
  maxWidth: 420,
  margin: "0 auto",
  boxShadow: "0 1px 4px rgba(0,0,0,0.06)",
};
const field: React.CSSProperties = {
  width: "100%",
  padding: "12px 14px",
  borderRadius: 10,
  border: "1px solid #d4dde4",
  fontSize: 16,
  marginBottom: 10,
  boxSizing: "border-box",
};
const primaryBtn: React.CSSProperties = {
  width: "100%",
  padding: "14px",
  borderRadius: 10,
  border: "none",
  background: "#36a3e0",
  color: "#fff",
  fontSize: 16,
  fontWeight: 600,
  cursor: "pointer",
};

export default function Home() {
  const [session, setSession] = useState<Session | null>(null);
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [msg, setMsg] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    supabase.auth.getSession().then(({ data }) => setSession(data.session));
    const { data: sub } = supabase.auth.onAuthStateChange((_e, s) => setSession(s));
    return () => sub.subscription.unsubscribe();
  }, []);

  async function signInEmail() {
    setBusy(true);
    setMsg(null);
    const { error } = await supabase.auth.signInWithPassword({ email: email.trim().toLowerCase(), password });
    if (error) setMsg(error.message);
    setBusy(false);
  }

  async function signInGoogle() {
    setMsg(null);
    await supabase.auth.signInWithOAuth({
      provider: "google",
      options: { redirectTo: typeof window !== "undefined" ? window.location.origin : undefined },
    });
  }

  async function checkout(plan: PlanKey | OneTimeKey, kind: "subscription" | "onetime" = "subscription") {
    if (!session) return;
    setBusy(true);
    setMsg(null);
    try {
      const res = await fetch("/api/checkout", {
        method: "POST",
        headers: { "content-type": "application/json" },
        body: JSON.stringify({ plan, kind, accessToken: session.access_token }),
      });
      const json = await res.json();
      if (json.url) {
        window.location.href = json.url;
      } else {
        setMsg(json.error ?? "エラーが発生しました。");
        setBusy(false);
      }
    } catch {
      setMsg("ネットワークエラーが発生しました。");
      setBusy(false);
    }
  }

  return (
    <main style={{ padding: "48px 16px" }}>
      <div style={{ textAlign: "center", marginBottom: 28 }}>
        <div style={{ fontSize: 34, fontWeight: 800 }}>Askoo プレミアム</div>
        <div style={{ color: "#5a6b78", marginTop: 6 }}>ブラウザでプレミアムを購読する</div>
      </div>

      <div style={{ maxWidth: 420, margin: "0 auto 24px", color: "#3a4a56", fontSize: 14, lineHeight: 1.8 }}>
        <p style={{ margin: "0 0 10px" }}>
          <strong>Askoo（アスク）</strong>は、学生のための質問AIアプリです。教科書や問題集を撮影するか、
          テキストで入力するだけで、AIが解き方を順を追って解説します。わからない部分は「深掘り」で
          さらに詳しく質問でき、学習の記録・復習もできます。
        </p>
        <p style={{ margin: 0 }}>
          プレミアムに登録すると、1日あたりの質問回数の上限が広がり、広告なしで利用できます。料金・更新・
          解約の詳細は<a href="/pricing" style={{ color: "#36a3e0" }}>料金ページ</a>をご覧ください。
        </p>
      </div>

      {!session ? (
        <div style={card}>
          <h2 style={{ fontSize: 18, marginTop: 0 }}>ログイン</h2>
          <p style={{ color: "#5a6b78", fontSize: 13, marginTop: 0 }}>
            アプリと同じアカウントでログインしてください。
          </p>
          <button style={{ ...primaryBtn, background: "#fff", color: "#1f2933", border: "1px solid #d4dde4", marginBottom: 14 }} onClick={signInGoogle}>
            Google でログイン
          </button>
          <input style={field} type="email" placeholder="メールアドレス" value={email} onChange={(e) => setEmail(e.target.value)} />
          <input style={field} type="password" placeholder="パスワード" value={password} onChange={(e) => setPassword(e.target.value)} />
          <button style={primaryBtn} onClick={signInEmail} disabled={busy || !email || !password}>
            {busy ? "..." : "メールでログイン"}
          </button>
        </div>
      ) : (
        <div style={card}>
          <p style={{ color: "#5a6b78", fontSize: 13, marginTop: 0 }}>
            ログイン中: {session.user.email ?? session.user.id}
          </p>
          <h2 style={{ fontSize: 18 }}>プランを選ぶ</h2>
          <p style={{ fontSize: 13, color: "#5a6b78", margin: "0 0 10px" }}>自動更新サブスク（カード / Apple Pay）</p>
          {(Object.keys(PLANS) as PlanKey[]).map((k) => (
            <button key={k} style={{ ...primaryBtn, marginBottom: 12 }} onClick={() => checkout(k)} disabled={busy}>
              {PLANS[k].label}（{PLANS[k].note}）
            </button>
          ))}
          <hr style={{ border: "none", borderTop: "1px solid #e4edf3", margin: "16px 0 12px" }} />
          <p style={{ fontSize: 13, color: "#5a6b78", margin: "0 0 4px" }}>PayPay / 都度払い（自動更新なし）</p>
          <p style={{ fontSize: 12, color: "#8a9baa", margin: "0 0 10px" }}>PayPayで購入できます。期間終了後は自動更新されません。</p>
          {(Object.keys(ONE_TIME_PLANS) as OneTimeKey[]).map((k) => (
            <button key={k} style={{ ...primaryBtn, marginBottom: 12, background: "#2f8a2f" }} onClick={() => checkout(k, "onetime")} disabled={busy}>
              {ONE_TIME_PLANS[k].label}（{ONE_TIME_PLANS[k].note}）
            </button>
          ))}
          <button
            style={{ ...primaryBtn, background: "transparent", color: "#5a6b78", fontWeight: 400 }}
            onClick={() => supabase.auth.signOut()}
          >
            ログアウト
          </button>
        </div>
      )}

      {msg && <p style={{ textAlign: "center", color: "#d14343", marginTop: 16 }}>{msg}</p>}
    </main>
  );
}
