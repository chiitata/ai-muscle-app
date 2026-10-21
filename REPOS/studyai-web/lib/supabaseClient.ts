"use client";
import { createClient } from "@supabase/supabase-js";

// ビルド時に env 未設定でも createClient が throw しないよう fallback を持つ。
// Vercel prod では実 env が注入されるためプレースホルダーが呼ばれることはない。
export const supabase = createClient(
  process.env.NEXT_PUBLIC_SUPABASE_URL ?? "https://placeholder.supabase.co",
  process.env.NEXT_PUBLIC_SUPABASE_ANON_KEY ?? "placeholder-anon-key",
);
