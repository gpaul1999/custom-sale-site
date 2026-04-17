/* =============================================
   CART – cookie-based shopping cart
   ============================================= */

// ── Cookie helpers ──────────────────────────────────────────────────────────
function _getCookie(name) {
  const v = document.cookie.match('(^|;)\\s*' + name + '\\s*=\\s*([^;]+)');
  return v ? decodeURIComponent(v.pop()) : null;
}

function _setCookie(name, value, days) {
  const d = new Date();
  d.setTime(d.getTime() + days * 86400000);
  document.cookie = name + '=' + encodeURIComponent(value)
      + ';expires=' + d.toUTCString() + ';path=/';
}

function _deleteCookie(name) {
  document.cookie = name + '=;expires=Thu, 01 Jan 1970 00:00:00 GMT;path=/';
}

// ── Cart read / write ────────────────────────────────────────────────────────
function cartRead() {
  try {
    const raw = _getCookie('cart');
    return raw ? JSON.parse(raw) : [];
  } catch (e) { return []; }
}

function cartWrite(items) {
  if (!items || items.length === 0) { _deleteCookie('cart'); }
  else { _setCookie('cart', JSON.stringify(items), 7); }
}

// ── Core cart operations (local cookie only, then sync server) ────────────────
function cartAdd(productId, qty) {
  qty = Math.max(1, qty || 1);
  const items = cartRead();
  const found = items.find(i => i.productId === productId);
  if (found) found.quantity += qty;
  else items.push({ productId, quantity: qty });
  cartWrite(items);
  _notifyCartUpdate(items);
}

function cartUpdate(productId, qty) {
  const items = cartRead();
  if (qty <= 0) {
    cartRemove(productId); return;
  }
  const found = items.find(i => i.productId === productId);
  if (found) found.quantity = qty;
  cartWrite(items);
  _notifyCartUpdate(items);
}

function cartRemove(productId) {
  const items = cartRead().filter(i => i.productId !== productId);
  cartWrite(items);
  _notifyCartUpdate(items);
}

function clearCart() {
  if (!confirm('Bạn có chắc muốn xóa toàn bộ giỏ hàng?')) return;
  cartWrite([]);
  _notifyCartUpdate([]);
  renderCartPage();
}


// ── Header badge + toast ────────────────────────────────────────────────────
function _notifyCartUpdate(items) {
  updateCartBadge(items);
  if (typeof renderCartPage === 'function' && document.getElementById('cart-items-container')) {
    renderCartPage();
  }
}

function updateCartBadge(items) {
  items = items || cartRead();
  const total = items.reduce((s, i) => s + i.quantity, 0);
  document.querySelectorAll('.cart-badge').forEach(el => {
    el.textContent = total;
    el.style.display = total > 0 ? 'flex' : 'none';
  });
}

// ── Format helpers ───────────────────────────────────────────────────────────
function fmt(n) {
  return new Intl.NumberFormat('vi-VN').format(Math.round(n || 0)) + ' đ';
}

// ── Toast notification ───────────────────────────────────────────────────────
function showToast(msg, type) {
  let container = document.getElementById('cart-toast-container');
  if (!container) {
    container = document.createElement('div');
    container.id = 'cart-toast-container';
    document.body.appendChild(container);
  }
  const toast = document.createElement('div');
  toast.className = 'cart-toast cart-toast-' + (type || 'success');
  toast.innerHTML = msg;
  container.appendChild(toast);
  setTimeout(() => toast.classList.add('show'), 10);
  setTimeout(() => {
    toast.classList.remove('show');
    setTimeout(() => toast.remove(), 300);
  }, 2800);
}

// ── Add to cart button handler (called from product pages) ───────────────────
function addToCartBtn(productId, productName, qty) {
  cartAdd(productId, qty || 1);
  showToast('🛒 <strong>' + (productName || 'Sản phẩm') + '</strong> đã được thêm vào giỏ hàng!');
}

// ── Cart page renderer ───────────────────────────────────────────────────────
async function renderCartPage() {
  const loadingEl   = document.getElementById('cart-loading');
  const emptyEl     = document.getElementById('cart-empty');
  const listEl      = document.getElementById('cart-items-list');
  const containerEl = document.getElementById('cart-items-container');
  const summaryEl   = document.getElementById('cart-summary');
  if (!containerEl) return; // not on cart page

  const items = cartRead();

  if (loadingEl) loadingEl.style.display = 'none';

  if (!items || items.length === 0) {
    if (emptyEl)  emptyEl.style.display  = 'flex';
    if (listEl)   listEl.style.display   = 'none';
    if (summaryEl) summaryEl.style.display = 'none';
    updateCartBadge([]);
    return;
  }

  if (emptyEl) emptyEl.style.display = 'none';

  // Fetch product info from server
  const ids = items.map(i => i.productId);
  let products = [];
  try {
    const res = await fetch('/api/cart/products', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(ids)
    });
    products = await res.json();
  } catch (e) {
    products = [];
  }

  // Build map productId -> product
  const pMap = {};
  products.forEach(p => { pMap[p.id] = p; });

  const tpl = document.getElementById('cart-item-tpl');
  containerEl.innerHTML = '';

  let sumOriginal = 0;
  let sumFinal    = 0;
  let hasVat      = false;
  let totalQty    = 0;

  items.forEach(item => {
    const p = pMap[item.productId];
    if (!p) return;

    const unitPrice    = p.saleOff && p.salePrice ? p.salePrice : p.price;
    const unitOriginal = p.price;
    const subtotal     = unitPrice ? unitPrice * item.quantity : 0;
    const subOriginal  = unitOriginal ? unitOriginal * item.quantity : 0;

    sumOriginal += subOriginal;
    sumFinal    += subtotal;
    totalQty    += item.quantity;

    const row = tpl.content.cloneNode(true);
    const el  = row.querySelector('.cart-item');
    el.setAttribute('data-product-id', p.id);

    // Image
    const img    = el.querySelector('.cart-item-img');
    const imgPh  = el.querySelector('.cart-item-img-placeholder');
    if (p.images && p.images.length > 0) {
      img.src = p.images[0];
      img.alt = p.syntax;
      img.onerror = () => { img.style.display = 'none'; imgPh.style.display = 'flex'; };
    } else {
      img.style.display = 'none';
      imgPh.style.display = 'flex';
    }

    // Info
    el.querySelector('.cart-item-name').textContent = p.syntax || '';
    el.querySelector('.cart-item-desc').textContent = p.description || '';

    // Sale badge
    const saleBadge = el.querySelector('.cart-item-badge-sale');
    if (p.saleOff && p.salePercent) {
      saleBadge.textContent = '-' + p.salePercent + '%';
      saleBadge.style.display = 'inline-flex';
    }

    // Unit price
    const unitPriceEl = el.querySelector('.cart-item-unit-price');
    if (p.saleOff && p.salePrice) {
      unitPriceEl.innerHTML =
          '<span class="ci-price-orig">' + fmt(p.price) + '</span>' +
          '<span class="ci-price-sale">' + fmt(p.salePrice) + '</span>';
    } else if (p.price) {
      unitPriceEl.innerHTML = '<span class="ci-price-normal">' + fmt(p.price) + '</span>';
    } else {
      unitPriceEl.innerHTML = '<span class="ci-price-contact">Liên hệ</span>';
    }

    // Quantity
    const qtyInput = el.querySelector('.qty-input');
    qtyInput.value = item.quantity;
    el.querySelector('.qty-minus').addEventListener('click', () => {
      const v = parseInt(qtyInput.value) - 1;
      if (v <= 0) { cartRemove(p.id); } else { cartUpdate(p.id, v); }
    });
    el.querySelector('.qty-plus').addEventListener('click', () => {
      cartUpdate(p.id, parseInt(qtyInput.value) + 1);
    });
    qtyInput.addEventListener('change', () => {
      const v = parseInt(qtyInput.value);
      if (isNaN(v) || v <= 0) cartRemove(p.id);
      else cartUpdate(p.id, v);
    });

    // Subtotal
    el.querySelector('.cart-item-subtotal').textContent = subtotal ? fmt(subtotal) : 'Liên hệ';

    // Remove
    el.querySelector('.btn-remove').addEventListener('click', () => cartRemove(p.id));

    containerEl.appendChild(row);
  });

  if (listEl)    listEl.style.display    = '';
  if (summaryEl) summaryEl.style.display = '';

  // Summary
  const discount = sumOriginal - sumFinal;
  document.getElementById('sum-count').textContent    = totalQty;
  document.getElementById('sum-original').textContent = fmt(sumOriginal);
  document.getElementById('sum-total').textContent    = fmt(sumFinal);

  const discRow = document.getElementById('sum-discount-row');
  if (discount > 0) {
    document.getElementById('sum-discount').textContent = '-' + fmt(discount);
    discRow.style.display = '';
  } else {
    discRow.style.display = 'none';
  }

  updateCartBadge(items);
}

// ── Init on every page load ──────────────────────────────────────────────────
document.addEventListener('DOMContentLoaded', () => {
  updateCartBadge();
});

