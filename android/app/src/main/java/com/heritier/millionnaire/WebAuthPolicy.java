package com.heritier.millionnaire;

import java.net.URI;
import java.net.URISyntaxException;

/** Pure policy shared by the native WebViews; deliberately has no Android dependencies. */
final class WebAuthPolicy {
    static final String DRAG_URL = "file:///android_asset/public/drag/index.html";
    private static final String CLIENT_HOST = "client-jeux-millionnaire.vercel.app";

    private WebAuthPolicy() {}

    static boolean isDragUrl(String value) {
        try {
            URI uri = new URI(value);
            return "file".equals(uri.getScheme()) && uri.getRawAuthority() == null
                    && "/android_asset/public/drag/index.html".equals(uri.getRawPath());
        } catch (URISyntaxException | NullPointerException error) {
            return false;
        }
    }

    static boolean isClientUrl(String value) {
        try {
            URI uri = new URI(value);
            return "https".equalsIgnoreCase(uri.getScheme())
                    && CLIENT_HOST.equalsIgnoreCase(uri.getHost())
                    && uri.getRawUserInfo() == null && (uri.getPort() == -1 || uri.getPort() == 443);
        } catch (URISyntaxException | NullPointerException error) {
            return false;
        }
    }

    static String fetchScript(String escapedToken, String apiBase) {
        // apiBase is a compile-time HTTPS URL. Tokens are already escaped by the caller.
        return """
            (function(){
              if(window.__hmFetchWrapped) return;
              window.__hmFetchWrapped=true;
              const original=window.fetch.bind(window);
              const allowed=new URL('%s');
              window.fetch=function(input,init){
                let target;
                try { target=new URL(input instanceof Request ? input.url : String(input),location.href); }
                catch (_) { return original(input,init); }
                if(target.origin!==allowed.origin || target.username || target.password)
                  return original(input,init);
                const options=Object.assign({},init);
                const headers=new Headers(options.headers || (input instanceof Request ? input.headers : undefined));
                if(!headers.has('Authorization')) headers.set('Authorization','Bearer %s');
                options.headers=headers;
                // Stop same-origin redirects from forwarding credentials to another endpoint.
                options.redirect='error';
                return original(input,options);
              };
            })();
            """.formatted(apiBase, escapedToken);
    }
}
