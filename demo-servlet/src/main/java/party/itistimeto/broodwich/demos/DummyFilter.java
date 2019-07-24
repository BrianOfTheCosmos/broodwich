package party.itistimeto.broodwich.demos;

import javax.servlet.*;
import java.io.IOException;

public class DummyFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        System.out.println("hello");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        System.out.println("hello");
    }

    @Override
    public void destroy() {

    }
}
