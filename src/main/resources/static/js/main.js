document.addEventListener('DOMContentLoaded', function () {

  // ── Mobile navigation toggle ──────────────────────────────────────────────
  var toggle = document.getElementById('navToggle');
  var menu   = document.getElementById('navMenu');

  if (toggle && menu) {
    toggle.addEventListener('click', function () {
      menu.classList.toggle('open');
      // Animate hamburger → X
      toggle.classList.toggle('is-open');
    });
    menu.querySelectorAll('a').forEach(function (link) {
      link.addEventListener('click', function () {
        menu.classList.remove('open');
        toggle.classList.remove('is-open');
      });
    });
    document.addEventListener('click', function (e) {
      if (!menu.contains(e.target) && !toggle.contains(e.target)) {
        menu.classList.remove('open');
        toggle.classList.remove('is-open');
      }
    });
  }

  // ── Services page: Tab switching is now server-side (URL-driven), no JS needed ──

  // ── Gallery image switcher ────────────────────────────────────────────────
  window.switchImg = function (thumb, src) {
    var mainImg = document.getElementById('mainImg');
    if (mainImg) mainImg.src = src;
    document.querySelectorAll('.gallery-thumb').forEach(function (t) {
      t.classList.remove('active');
    });
    thumb.classList.add('active');
  };

  // ── Scroll-reveal animation ───────────────────────────────────────────────
  var revealEls = document.querySelectorAll(
    '.card, .icon-card, .product-card, .testimonial-card, .service-mini-card, .hero-stat-card, .stat-item, .value-card, .contact-info-card, .team-card'
  );

  if ('IntersectionObserver' in window) {
    var io = new IntersectionObserver(function (entries) {
      entries.forEach(function (entry) {
        if (entry.isIntersecting) {
          entry.target.style.opacity = '1';
          entry.target.style.transform = 'translateY(0)';
          io.unobserve(entry.target);
        }
      });
    }, { threshold: 0.1, rootMargin: '0px 0px -30px 0px' });

    revealEls.forEach(function (el) {
      el.style.opacity = '0';
      el.style.transform = 'translateY(18px)';
      el.style.transition = 'opacity 0.45s ease, transform 0.45s ease';
      io.observe(el);
    });
  }

  // ── Smooth counter animation for stats ───────────────────────────────────
  var statNums = document.querySelectorAll('.stat-item .num');
  if ('IntersectionObserver' in window && statNums.length > 0) {
    var statsObserver = new IntersectionObserver(function (entries, obs) {
      entries.forEach(function (entry) {
        if (entry.isIntersecting) {
          animateCount(entry.target);
          obs.unobserve(entry.target);
        }
      });
    }, { threshold: 0.5 });
    statNums.forEach(function (el) { statsObserver.observe(el); });
  }

  function animateCount(el) {
    var raw = el.textContent.trim();
    var num = parseFloat(raw.replace(/[^0-9.]/g, ''));
    if (isNaN(num) || num === 0) return;
    var suffix = raw.replace(/[0-9.,]/g, '');
    var duration = 1200;
    var steps = 40;
    var step = 0;
    var timer = setInterval(function () {
      step++;
      var progress = step / steps;
      var eased = 1 - Math.pow(1 - progress, 3);
      var current = num * eased;
      if (Number.isInteger(num)) {
        el.textContent = Math.floor(current).toLocaleString('vi-VN') + suffix;
      } else {
        el.textContent = current.toFixed(1) + suffix;
      }
      if (step >= steps) {
        clearInterval(timer);
        el.textContent = raw;
      }
    }, duration / steps);
  }

});

