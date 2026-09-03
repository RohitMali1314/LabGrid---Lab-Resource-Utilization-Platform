import { createFileRoute, Link, useNavigate } from "@tanstack/react-router";
import { useEffect, useState } from "react";
import { GoogleLogin } from "@react-oauth/google";
import { toast } from "sonner";
import { AuthShell } from "@/components/auth-shell";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { useAuth, ROLE_HOME } from "@/lib/auth";
import { apiErrorKind, apiErrorMessage } from "@/services/api";
import { Loader2 } from "lucide-react";

export const Route = createFileRoute("/login")({ component: Login });

/** Clean, non-technical message for any Google sign-in failure. */
function googleErrorMessage(err: unknown): string {
  switch (apiErrorKind(err)) {
    case "auth":
      return "Google authentication failed.";
    case "forbidden":
      return "Google sign-in could not be completed.";
    case "unavailable":
      return "Google Sign-In is currently unavailable.";
    case "server":
      return "Google Sign-In is temporarily unavailable. Please try again later.";
    case "network":
      return "Unable to connect to the server. Please try again later.";
    default:
      return "Google authentication failed.";
  }
}


function GoogleIcon() {
  return (
    <svg viewBox="0 0 48 48" className="mr-2 h-4 w-4" aria-hidden="true">
      <path fill="#EA4335" d="M24 9.5c3.5 0 6.6 1.2 9 3.6l6.7-6.7C35.6 2.6 30.2 0 24 0 14.6 0 6.5 5.4 2.6 13.2l7.8 6.1C12.3 13.2 17.6 9.5 24 9.5z"/>
      <path fill="#4285F4" d="M46.5 24.5c0-1.6-.1-3.1-.4-4.5H24v9h12.7c-.6 3-2.3 5.5-4.9 7.2l7.6 5.9c4.4-4.1 7.1-10.1 7.1-17.6z"/>
      <path fill="#FBBC05" d="M10.4 28.7a14.6 14.6 0 0 1 0-9.4l-7.8-6.1a24 24 0 0 0 0 21.6l7.8-6.1z"/>
      <path fill="#34A853" d="M24 48c6.5 0 11.9-2.1 15.9-5.8l-7.6-5.9c-2.1 1.4-4.8 2.3-8.3 2.3-6.4 0-11.7-3.7-13.6-9.1l-7.8 6.1C6.5 42.6 14.6 48 24 48z"/>
    </svg>
  );
}

function Login() {
  const { login, loginWithGoogle, user, loading: authLoading } = useAuth();
  const navigate = useNavigate();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [googleBusy, setGoogleBusy] = useState(false);

  useEffect(() => {
    if (!authLoading && user) navigate({ to: ROLE_HOME[user.role], replace: true });
  }, [user, authLoading, navigate]);

  const onSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!email || !password) return toast.error("Please enter email and password");
    setSubmitting(true);
    try {
      const u = await login(email, password);
      toast.success(`Welcome back, ${u.name}`);
      navigate({ to: ROLE_HOME[u.role], replace: true });
    } catch (err) {
      toast.error(apiErrorMessage(err, "Invalid email or password"));
    } finally {
      setSubmitting(false);
    }
  };

  /**
   * Google Sign-In. The client id always comes from VITE_GOOGLE_CLIENT_ID —
   * never hardcoded, never a client secret. Without it the button degrades
   * gracefully instead of throwing.
   */
  const googleClientId = (import.meta.env.VITE_GOOGLE_CLIENT_ID as string | undefined) ?? "";

  const onGoogleCredential = async (credential?: string) => {
    if (googleBusy) return;
    if (!credential) {
      toast.error("Google authentication failed.");
      return;
    }
    setGoogleBusy(true);
    try {
      const u = await loginWithGoogle(credential);
      toast.success(`Welcome back, ${u.name}`);
      navigate({ to: ROLE_HOME[u.role], replace: true });
    } catch (err) {
      toast.error(googleErrorMessage(err));
    } finally {
      setGoogleBusy(false);
    }
  };

  return (
    <AuthShell title="Sign in" subtitle="Access your lab resource dashboard">
      <form onSubmit={onSubmit} className="space-y-4">
        <div className="space-y-2">
          <Label htmlFor="email">Work email</Label>
          <Input id="email" type="email" autoComplete="email" placeholder="you@institution.edu"
            value={email} onChange={(e) => setEmail(e.target.value)} required />
        </div>
        <div className="space-y-2">
          <div className="flex items-center justify-between">
            <Label htmlFor="password">Password</Label>
            <Link to="/forgot-password" className="text-xs text-primary hover:underline">Forgot?</Link>
          </div>
          <Input id="password" type="password" autoComplete="current-password" placeholder="••••••••"
            value={password} onChange={(e) => setPassword(e.target.value)} required />
        </div>
        <Button type="submit" className="w-full" disabled={submitting}>
          {submitting && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
          Sign in
        </Button>

        <div className="relative py-1">
          <div className="absolute inset-0 flex items-center"><span className="w-full border-t" /></div>
          <div className="relative flex justify-center">
            <span className="bg-card px-2 text-xs uppercase tracking-wide text-muted-foreground">or</span>
          </div>
        </div>

        {/* Existing outline button appearance kept; Google's own button is overlaid
            transparently so we still receive a real Google ID credential. */}
        <div className="relative w-full">
          <Button
            type="button"
            variant="outline"
            className="pointer-events-none w-full"
            disabled={googleBusy || !googleClientId}
          >
            {googleBusy ? <Loader2 className="mr-2 h-4 w-4 animate-spin" /> : <GoogleIcon />}
            {googleBusy ? "Connecting to Google..." : "Continue with Google"}
          </Button>
          {googleClientId && !googleBusy && (
            <div className="absolute inset-0 overflow-hidden opacity-0 [color-scheme:light]">
              <GoogleLogin
                width="400"
                onSuccess={(res) => void onGoogleCredential(res.credential)}
                onError={() => toast.error("Google authentication failed.")}
              />
            </div>
          )}
        </div>
        {!googleClientId && (
          <p className="text-center text-[11px] text-muted-foreground">
            Google Sign-In is currently unavailable.
          </p>
        )}


        <p className="text-center text-sm text-muted-foreground">
          New to LabGrid? <Link to="/register" className="text-primary hover:underline">Create an account</Link>
        </p>
      </form>
    </AuthShell>
  );
}

