export default function AppCancel() {
  return (
    <main style={{ padding: "64px 16px", textAlign: "center" }}>
      <div style={{ fontSize: 56 }}>↩</div>
      <h1 style={{ fontSize: 26 }}>購入をキャンセルしました</h1>
      <p style={{ color: "#5a6b78" }}>決済はキャンセルされました。アプリに戻って再度お試しください。</p>
      <p style={{ marginTop: 32 }}>
        <a
          href="studyai://return"
          style={{
            display: "inline-block",
            padding: "12px 28px",
            background: "#6b7280",
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
