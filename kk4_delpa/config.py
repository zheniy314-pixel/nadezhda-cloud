from __future__ import annotations

import os
from dataclasses import dataclass


def _read_bool(raw: str | None, default: bool) -> bool:
    if raw is None:
        return default
    v = raw.strip().lower()
    if v in {"1", "true", "yes", "on"}:
        return True
    if v in {"0", "false", "no", "off"}:
        return False
    return default


@dataclass(frozen=True)
class Settings:
    dry_run: bool
    target: str
    artifact: str | None


def load_from_env(env: dict[str, str] | None = None) -> Settings:
    e = env if env is not None else os.environ
    dry_run = _read_bool(e.get("KK4_DELPA_DRY_RUN"), default=True)
    target = (e.get("KK4_DELPA_TARGET") or "default").strip() or "default"
    artifact = e.get("KK4_DELPA_ARTIFACT")
    if artifact is not None:
        artifact = artifact.strip() or None
    return Settings(dry_run=dry_run, target=target, artifact=artifact)
