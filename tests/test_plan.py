import unittest

from kk4_delpa.config import load_from_env
from kk4_delpa.plan import build_plan


class PlanTests(unittest.TestCase):
    def test_default_dry_run(self) -> None:
        s = load_from_env(
            {
                "KK4_DELPA_DRY_RUN": "true",
                "KK4_DELPA_TARGET": "staging",
            }
        )
        self.assertTrue(s.dry_run)
        plan = build_plan(s)
        self.assertGreaterEqual(len(plan), 3)
        self.assertIn("DRY-RUN", plan[-1].detail)

    def test_bool_parsing(self) -> None:
        s = load_from_env({"KK4_DELPA_DRY_RUN": "false"})
        self.assertFalse(s.dry_run)


if __name__ == "__main__":
    unittest.main()
