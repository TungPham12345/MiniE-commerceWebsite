package controller;

import dao.ProductDAO;
import model.CartItem;
import model.Product;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "CartController", urlPatterns = {"/cart"})
public class CartController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("cart.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        request.setCharacterEncoding("UTF-8");

        String action = request.getParameter("action");
        HttpSession session = request.getSession();

        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
        if (cart == null) {
            cart = new ArrayList<>();
        }

        ProductDAO productDAO = new ProductDAO();
        CartItem addedItem = null;

        if ("add".equals(action)) {
            int productId = Integer.parseInt(request.getParameter("productId"));
            int quantity = Integer.parseInt(request.getParameter("quantity"));
            String size = request.getParameter("size");
            String color = request.getParameter("color");
            if (size == null || size.trim().isEmpty()) size = "35";
            if (color == null || color.trim().isEmpty()) color = "Đen";

            Product p = productDAO.getProductById(productId);
            if (p != null) {
                boolean exists = false;
                for (CartItem item : cart) {
                    if (item.getProduct().getId() == productId 
                        && (size.equals(item.getSize())) 
                        && (color.equals(item.getColor()))) {
                        item.setQuantity(item.getQuantity() + quantity);
                        addedItem = item;
                        exists = true;
                        break;
                    }
                }
                if (!exists) {
                    addedItem = new CartItem(p, quantity, size, color);
                    cart.add(addedItem);
                }
            }

            double totalMoney = 0;
            for (CartItem item : cart) {
                totalMoney += item.getTotalPrice();
            }

            session.setAttribute("cart", cart);
            session.setAttribute("cartTotal", totalMoney);
            session.setAttribute("cartSize", cart.size());
            session.setAttribute("addedToCart", true);
            session.setAttribute("lastAddedItem", addedItem);

            response.sendRedirect("product-detail?id=" + productId);
            return;
        } else if ("update".equals(action)) {
            int productId = Integer.parseInt(request.getParameter("productId"));
            int quantity = Integer.parseInt(request.getParameter("quantity"));
            for (CartItem item : cart) {
                if (item.getProduct().getId() == productId) {
                    if (quantity <= 0) {
                        cart.remove(item);
                    } else {
                        item.setQuantity(quantity);
                    }
                    break;
                }
            }
        } else if ("remove".equals(action)) {
            int productId = Integer.parseInt(request.getParameter("productId"));
            cart.removeIf(item -> item.getProduct().getId() == productId);
        }

        double totalMoney = 0;
        for (CartItem item : cart) {
            totalMoney += item.getTotalPrice();
        }

        session.setAttribute("cart", cart);
        session.setAttribute("cartTotal", totalMoney);
        session.setAttribute("cartSize", cart.size());

        response.sendRedirect("cart");
    }
}
