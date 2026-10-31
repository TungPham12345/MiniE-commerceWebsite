<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>HighFashion - Thời Trang Thượng Lưu & Phong Cách</title>
    <!-- Dark Mode Initializer: Khởi tạo chủ đề Sáng / Tối ngay lập tức để chống chớp giật màn hình (No FOUT) -->
    <script>
        (function() {
            try {
                const savedTheme = localStorage.getItem('theme');
                const systemPrefersDark = window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches;
                const activeTheme = savedTheme ? savedTheme : (systemPrefersDark ? 'dark' : 'light');
                document.documentElement.setAttribute('data-bs-theme', activeTheme);
            } catch (e) {
                // Fallback nếu trình duyệt chặn localStorage
                document.documentElement.setAttribute('data-bs-theme', 'light');
            }
        })();
    </script>
    <!-- Tự động chuyển hướng về Trang chủ khi người dùng bấm Refresh (F5 / Reload) ở bất kỳ trang con nào, nhưng KHÔNG chặn nút Back/Forward -->
    <script>
        (function() {
            try {
                const navEntries = (window.performance && performance.getEntriesByType) ? performance.getEntriesByType('navigation') : null;
                const navType = (navEntries && navEntries.length > 0) ? navEntries[0].type : '';
                const legacyType = (window.performance && window.performance.navigation) ? window.performance.navigation.type : -1;
                
                // Kiểm tra nếu là thao tác bấm nút Quay Lại (Back) hoặc Tiến (Forward) của trình duyệt
                const isBackForward = navType === 'back_forward' || legacyType === 2;
                if (isBackForward) {
                    return; // Cho phép quay lại trang trước để tiếp tục mua sắm
                }

                const isReload = (navType === 'reload') || (legacyType === 1);
                if (isReload) {
                    const currentPath = window.location.pathname;
                    const contextPath = '${pageContext.request.contextPath}';
                    const homeUrl = contextPath + '/home';
                    
                    const isHomePage = currentPath === contextPath ||
                                       currentPath === contextPath + '/' ||
                                       currentPath === contextPath + '/home' ||
                                       currentPath === contextPath + '/index.jsp';
                    const isAdminPage = currentPath.includes('/admin');

                    if (!isHomePage && !isAdminPage) {
                        window.location.replace(homeUrl);
                    }
                }

                // Lắng nghe phím tắt F5 hoặc Ctrl+R
                window.addEventListener('keydown', function(e) {
                    if (e.key === 'F5' || (e.ctrlKey && (e.key === 'r' || e.key === 'R'))) {
                        const currentPath = window.location.pathname;
                        const contextPath = '${pageContext.request.contextPath}';
                        const homeUrl = contextPath + '/home';
                        const isHomePage = currentPath === contextPath ||
                                           currentPath === contextPath + '/' ||
                                           currentPath === contextPath + '/home' ||
                                           currentPath === contextPath + '/index.jsp';
                        const isAdminPage = currentPath.includes('/admin');

                        if (!isHomePage && !isAdminPage) {
                            e.preventDefault();
                            window.location.replace(homeUrl);
                        }
                    }
                });
            } catch (e) {
                console.error('Refresh redirect error:', e);
            }
        })();
    </script>
    <!-- Bootstrap 5 CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <!-- Bootstrap Icons -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.0/font/bootstrap-icons.css" rel="stylesheet">
    <!-- Custom CSS -->
    <link href="${pageContext.request.contextPath}/css/style.css" rel="stylesheet">
</head>
<body>
