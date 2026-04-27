(function () {
  var yearEl = document.getElementById("year");
  if (yearEl) {
    yearEl.textContent = String(new Date().getFullYear());
  }

  var toggle = document.querySelector(".nav-toggle");
  var panel = document.getElementById("nav-panel");

  if (toggle && panel) {
    toggle.addEventListener("click", function () {
      var open = panel.classList.toggle("is-open");
      toggle.setAttribute("aria-expanded", open ? "true" : "false");
    });

    panel.querySelectorAll('a[href^="#"]').forEach(function (link) {
      link.addEventListener("click", function () {
        panel.classList.remove("is-open");
        toggle.setAttribute("aria-expanded", "false");
      });
    });

    document.addEventListener("click", function (e) {
      if (!panel.contains(e.target) && !toggle.contains(e.target)) {
        panel.classList.remove("is-open");
        toggle.setAttribute("aria-expanded", "false");
      }
    });
  }
})();
