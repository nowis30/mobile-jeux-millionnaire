package com.heritier.millionnaire;

public class WebAuthPolicyTest {
    private static int checks;
    private static void expect(boolean value, boolean expected, String label) {
        checks++;
        if (value != expected) throw new AssertionError(label);
    }
    public static void main(String[] args) {
        if (args.length > 0) { System.out.print(WebAuthPolicy.fetchScript("test-token", "https://server-jeux-millionnaire.onrender.com")); return; }
        expect(WebAuthPolicy.isDragUrl(WebAuthPolicy.DRAG_URL), true, "local drag");
        expect(WebAuthPolicy.isDragUrl(WebAuthPolicy.DRAG_URL + "?returnTo=x#menu"), true, "local query");
        for (String uri : new String[]{null, "https://evil.example/", "file:///etc/passwd", "file:///android_asset/public/drag/../index.html", "file:///android_asset/public/drag/%69ndex.html", "file://evil/android_asset/public/drag/index.html", "javascript:alert(1)", "content://x/index.html"})
            expect(WebAuthPolicy.isDragUrl(uri), false, "reject drag " + uri);
        expect(WebAuthPolicy.isClientUrl("https://client-jeux-millionnaire.vercel.app/immobilier"), true, "client");
        expect(WebAuthPolicy.isClientUrl("https://client-jeux-millionnaire.vercel.app:443/"), true, "tls port");
        for (String uri : new String[]{null, "http://client-jeux-millionnaire.vercel.app/", "https://client-jeux-millionnaire.vercel.app.evil.test/", "https://client-jeux-millionnaire.vercel.app@evil.test/", "https://evil@client-jeux-millionnaire.vercel.app/", "https://client-jeux-millionnaire.vercel.app:8443/", "javascript:alert(1)", "//client-jeux-millionnaire.vercel.app/"})
            expect(WebAuthPolicy.isClientUrl(uri), false, "reject client " + uri);
        System.out.println(checks + " URI policy checks passed");
    }
}
