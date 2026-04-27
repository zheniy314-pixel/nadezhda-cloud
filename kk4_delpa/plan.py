from __future__ import annotations

import os
from dataclasses import dataclass

from .config import Settings


@dataclass(frozen=True)
class PlanStep:
    order: int
    title: str
    detail: str


def build_plan(settings: Settings) -> list[PlanStep]:
    steps: list[PlanStep] = [
        PlanStep(1, "Проверка окружения", "Загружены настройки из переменных среды"),
        PlanStep(
            2,
            "Проверка артефакта",
            _artifact_message(settings.artifact),
        ),
        PlanStep(
            3,
            "Публикация",
            _publish_message(settings),
        ),
    ]
    return steps


def _artifact_message(path: str | None) -> str:
    if not path:
        return "Артефакт не задан (KK4_DELPA_ARTIFACT) — шаг опционален"
    if os.path.exists(path):
        kind = "каталог" if os.path.isdir(path) else "файл"
        return f"Найден {kind}: {path}"
    return f"Путь не найден: {path} (будет ошибка при реальном деплое)"


def _publish_message(settings: Settings) -> str:
    if settings.dry_run:
        return (
            f"DRY-RUN: имитация выката в «{settings.target}» без сети и без секретов"
        )
    return (
        f"Реальный выкат в «{settings.target}» не реализован в этой версии; "
        "включите KK4_DELPA_DRY_RUN=true или добавьте свой backend."
    )
