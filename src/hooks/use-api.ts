import { useCallback, useEffect, useState } from "react";
import { apiErrorKind, apiErrorMessage, type ApiErrorKind } from "@/services/api";

export interface ApiState<T> {
  data: T | null;
  loading: boolean;
  error: string | null;
  errorKind: ApiErrorKind | null;
  reload: () => Promise<void>;
  setData: (d: T | null) => void;
}

export interface UseApiOptions {
  /** When false the request is never sent (e.g. the role isn't authorized). */
  enabled?: boolean;
}

/**
 * Fetch on mount, expose reload/setData.
 * The request is issued exactly once per dependency change — 403/404 responses are
 * never retried automatically, they surface as a clean error kind instead.
 */
export function useApi<T>(
  fn: () => Promise<T>,
  deps: React.DependencyList = [],
  options: UseApiOptions = {},
): ApiState<T> {
  const enabled = options.enabled !== false;
  const [data, setData] = useState<T | null>(null);
  const [loading, setLoading] = useState(enabled);
  const [error, setError] = useState<string | null>(null);
  const [errorKind, setErrorKind] = useState<ApiErrorKind | null>(null);

  const run = useCallback(async () => {
    if (!enabled) {
      setLoading(false);
      return;
    }
    setLoading(true);
    setError(null);
    setErrorKind(null);
    try {
      const result = await fn();
      setData(result);
    } catch (err) {
      if (import.meta.env.DEV) console.debug("[api]", err);
      setError(apiErrorMessage(err));
      setErrorKind(apiErrorKind(err));
      setData(null);
    } finally {
      setLoading(false);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [enabled, ...deps]);

  useEffect(() => {
    void run();
  }, [run]);

  return { data, loading, error, errorKind, reload: run, setData };
}
