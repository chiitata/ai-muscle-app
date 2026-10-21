import type { Metadata } from "next";

export const metadata: Metadata = {
  title: "メール認証 | Askoo",
  robots: { index: false, follow: false },
};

export default function AuthCallbackPage() {
  return (
    <main style={{ maxWidth: 520, margin: "64px auto", padding: 24, lineHeight: 1.8 }}>
      <h1>Askooアプリでメールを開いてください</h1>
      <p>
        認証リンクはAskooアプリで処理します。アプリが開かなかった場合は、
        アプリをインストールした後、届いたメールのリンクをもう一度開いてください。
      </p>
      <p>リンクの有効期限が切れている場合は、アプリから新しいメールを送信してください。</p>
    </main>
  );
}
