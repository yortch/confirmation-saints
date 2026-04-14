// Contact form configuration
// Email is encoded to prevent harvesting by bots/scrapers
const _c = [
  'Y29uZmlybWF0aW9uX3NhaW50c0',  // part 1
  'BvdXRsb29rLmNvbQ=='             // part 2
];
function _d() { return atob(_c[0] + _c[1]); }

document.addEventListener('DOMContentLoaded', function() {
  const form = document.getElementById('contact-form');
  if (!form) return;

  form.addEventListener('submit', function(e) {
    e.preventDefault();

    const name = form.querySelector('[name="name"]').value.trim();
    const type = form.querySelector('[name="type"]').value;
    const message = form.querySelector('[name="message"]').value.trim();

    if (!message) {
      alert('Please enter a message.');
      return;
    }

    const subject = encodeURIComponent(
      '[Confirmation Saints] ' + type + (name ? ' — from ' + name : '')
    );
    const body = encodeURIComponent(
      (name ? 'From: ' + name + '\n' : '') +
      'Type: ' + type + '\n\n' +
      message
    );

    window.location.href = 'mailto:' + _d() + '?subject=' + subject + '&body=' + body;

    // Show confirmation
    const btn = form.querySelector('button[type="submit"]');
    const original = btn.textContent;
    btn.textContent = '✓ Opening email client...';
    btn.disabled = true;
    setTimeout(function() {
      btn.textContent = original;
      btn.disabled = false;
    }, 3000);
  });
});
