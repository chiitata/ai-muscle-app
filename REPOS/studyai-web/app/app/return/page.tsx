type Props = { searchParams: Promise<{ session_id?: string }> };

export default async function AppReturn({ searchParams }: Props) {
  const { session_id } = await searchParams;
  return (
    <main style={{ padding: "64px 16px", textAlign: "center" }}>
      <div style={{ fontSize: 56 }}>✅</div>
      <h1 style={{ fontSize: 26 }}>購入が完了しました</h1>
      <p style={{ color: "#5a6b78" }}>
        アプリに戻ると、数秒以内にプレミアムが反映されます。
        <br />
        反映されない場合はアプリを再起動してください。
      </p>
      {session_id && (
        <p style={{ fontSize: 12, color: "#9ca3af", marginTop: 8 }}>
          セッション: {session_id.slice(0, 24)}…
        </p>
      )}
      <p style={{ marginTop: 32 }}>
        <a
          href="studyai://return"
          style={{
            display: "inline-block",
            padding: "12px 28px",
            background: "#3b82f6",
            color: "#fff",
            borderRadius: 8,
            textDecoration: "none",
            fontWeight: 600,
          }}
        >
          アプリに戻る
        </a>
      </p>
    </main>
  );
}
