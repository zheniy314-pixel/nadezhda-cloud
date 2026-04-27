from __future__ import annotations

import argparse
import sys

from . import __version__
from .config import load_from_env
from .plan import build_plan


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(
        prog="kk4-delpa",
        description="План деплоя (dry-run по умолчанию, без секретов в коде).",
    )
    parser.add_argument(
        "--version",
        action="version",
        version=f"%(prog)s {__version__}",
    )
    parser.add_argument(
        "--dry-run",
        action="store_true",
        help="Принудительно dry-run (переменная KK4_DELPA_DRY_RUN всё ещё учитывается при загрузке)",
    )
    args = parser.parse_args(argv)
    if args.dry_run:
        import os

        os.environ["KK4_DELPA_DRY_RUN"] = "true"

    settings = load_from_env()
    steps = build_plan(settings)

    lines = [
        f"KK4 Delpa {__version__}",
        f"target={settings.target} dry_run={settings.dry_run}",
        "",
        "План:",
    ]
    for s in steps:
        lines.append(f"  {s.order}. {s.title}: {s.detail}")

    print("\n".join(lines))
    return 0


if __name__ == "__main__":
    raise SystemExit(main(sys.argv[1:]))
