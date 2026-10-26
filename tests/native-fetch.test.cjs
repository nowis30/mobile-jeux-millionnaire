const assert = require('node:assert/strict');
const vm = require('node:vm');
const { execFileSync } = require('node:child_process');
const path = require('node:path');
const script = execFileSync('java', ['-cp', path.join(__dirname, '../.test-classes'), 'com.heritier.millionnaire.WebAuthPolicyTest', '--script'], { encoding: 'utf8' });
const calls = [];
const window = { fetch: (input, init) => { calls.push({ input, init }); return Promise.resolve(); } };
vm.runInNewContext(script, { window, URL, Request, Headers, location: { href: 'file:///android_asset/public/drag/index.html' } });
for (const address of ['https://untrusted.example/', 'http://server-jeux-millionnaire.onrender.com/api', 'https://server-jeux-millionnaire.onrender.com.evil.test/api', 'https://someone@server-jeux-millionnaire.onrender.com/api']) {
  window.fetch(address);
  assert.equal(calls.at(-1).init, undefined, 'must not attach credentials outside exact HTTPS origin');
}
window.fetch(new Request('https://server-jeux-millionnaire.onrender.com/api', { headers: { 'X-Custom': 'kept' } }));
assert.equal(calls.at(-1).init.headers.get('Authorization'), 'Bearer test-token');
assert.equal(calls.at(-1).init.headers.get('X-Custom'), 'kept');
assert.equal(calls.at(-1).init.redirect, 'error');
const headers = new Headers({ Authorization: 'Bearer existing' });
window.fetch('https://server-jeux-millionnaire.onrender.com/api', { headers });
assert.equal(calls.at(-1).init.headers.get('Authorization'), 'Bearer existing');
assert.equal(headers.get('Authorization'), 'Bearer existing');
console.log('8 native fetch policy assertions passed');
