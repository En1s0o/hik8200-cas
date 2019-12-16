package eniso.common.cookie.cache;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;

import okhttp3.Cookie;

/**
 * 默认的 cookie 缓存
 *
 * @author Eniso
 */
public class DefaultCookieCache implements CookieCache {

    private Set<IdentifiableCookie> cookies;

    public DefaultCookieCache() {
        cookies = new HashSet<>();
    }

    @Override
    public void addAll(Collection<Cookie> newCookies) {
        for (IdentifiableCookie cookie : IdentifiableCookie.decorateAll(newCookies)) {
            cookies.remove(cookie);
            cookies.add(cookie);
        }
    }

    @Override
    public Iterator<Cookie> iterator() {
        return new SetCookieCacheIterator();
    }

    private class SetCookieCacheIterator implements Iterator<Cookie> {

        private Iterator<IdentifiableCookie> iterator;

        private SetCookieCacheIterator() {
            Objects.requireNonNull(cookies);
            iterator = cookies.iterator();
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public Cookie next() {
            return iterator.next().getCookie();
        }

        @Override
        public void remove() {
            iterator.remove();
        }

    }

}
