package filter;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;
public class ContextRedirectFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        String uri = req.getRequestURI();
        if (uri.startsWith("/project_hotel/")) {
            String newUri = uri.substring("/project_hotel".length());
            res.sendRedirect(newUri);
            return;
        }
        chain.doFilter(request, response);
    }
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}
    @Override
    public void destroy() {}
}