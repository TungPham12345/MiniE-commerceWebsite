-- =====================================================================
-- DATABASE CHÍNH DỰ ÁN BÁN HÀNG THỜI TRANG (SQL SERVER T-SQL)
-- File duy nhat: database.sql
-- Thuc thi tren Microsoft SQL Server / SQL Server Management Studio (SSMS)
-- =====================================================================

IF NOT EXISTS (SELECT * FROM sys.databases WHERE name = 'ban_hang_db')
BEGIN
    CREATE DATABASE ban_hang_db;
END;
GO

USE ban_hang_db;
GO

-- Xoa bang neu da ton tai (Theo thu tu khoa ngoai)
IF OBJECT_ID('stores', 'U') IS NOT NULL DROP TABLE stores;
IF OBJECT_ID('order_details', 'U') IS NOT NULL DROP TABLE order_details;
IF OBJECT_ID('orders', 'U') IS NOT NULL DROP TABLE orders;
IF OBJECT_ID('products', 'U') IS NOT NULL DROP TABLE products;
IF OBJECT_ID('categories', 'U') IS NOT NULL DROP TABLE categories;
IF OBJECT_ID('users', 'U') IS NOT NULL DROP TABLE users;
GO

-- 1. Bang Danh muc san pham (Categories)
CREATE TABLE categories (
    id INT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(100) NOT NULL,
    description NVARCHAR(255),
    icon NVARCHAR(50) DEFAULT 'bi-tag'
);
GO

-- 2. Bang San pham (Products)
CREATE TABLE products (
    id INT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(255) NOT NULL,
    description NVARCHAR(MAX),
    price FLOAT NOT NULL,
    sale_price FLOAT DEFAULT 0,
    quantity INT DEFAULT 100,
    image NVARCHAR(500),
    size NVARCHAR(50) DEFAULT 'S, M, L, XL',
    color NVARCHAR(100) DEFAULT N'Trắng, Đen, Xanh',
    category_id INT FOREIGN KEY REFERENCES categories(id) ON DELETE CASCADE,
    is_featured BIT DEFAULT 0,
    created_at DATETIME DEFAULT GETDATE()
);
GO

-- 3. Bang Nguoi dung (Users)
CREATE TABLE users (
    id INT IDENTITY(1,1) PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    fullname NVARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    address NVARCHAR(255),
    role INT DEFAULT 0,
    created_at DATETIME DEFAULT GETDATE()
);
GO

-- 4. Bang Don hang (Orders)
CREATE TABLE orders (
    id INT IDENTITY(1,1) PRIMARY KEY,
    user_id INT NULL FOREIGN KEY REFERENCES users(id) ON DELETE SET NULL,
    customer_name NVARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    address NVARCHAR(255) NOT NULL,
    note NVARCHAR(MAX),
    total_money FLOAT NOT NULL,
    payment_method NVARCHAR(50) DEFAULT 'COD',
    status NVARCHAR(50) DEFAULT 'PENDING',
    created_at DATETIME DEFAULT GETDATE()
);
GO

-- 5. Bang Chi tiet don hang (OrderDetails)
CREATE TABLE order_details (
    id INT IDENTITY(1,1) PRIMARY KEY,
    order_id INT NOT NULL FOREIGN KEY REFERENCES orders(id) ON DELETE CASCADE,
    product_id INT NOT NULL FOREIGN KEY REFERENCES products(id) ON DELETE CASCADE,
    price FLOAT NOT NULL,
    quantity INT NOT NULL,
    size NVARCHAR(20),
    color NVARCHAR(20),
    total_money FLOAT NOT NULL
);
GO

-- 6. Bang He thong Ban do Cua hang (Stores)
CREATE TABLE stores (
    id INT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(255) NOT NULL,
    address NVARCHAR(255) NOT NULL,
    city NVARCHAR(100) NOT NULL,
    phone VARCHAR(50),
    hours NVARCHAR(100) DEFAULT '08:30 - 22:00',
    latitude FLOAT NOT NULL,
    longitude FLOAT NOT NULL,
    is_main BIT DEFAULT 0
);
GO

-- =====================================================================
-- DỮ LIỆU MẪU ĐẦY ĐỦ CHO TẤT CẢ BỘ LỌC
-- =====================================================================

-- Seed Categories
INSERT INTO categories (name, description, icon) VALUES
(N'Áo Nam', N'Áo thun, áo sơ mi, áo khoác nam thời trang', 'bi-person-badge'),
(N'Quần Nam', N'Quần jeans, quần tây, quần short nam', 'bi-gender-male'),
(N'Thời Trang Nữ', N'Váy, đầm, áo kiểu, quần nữ cá tính', 'bi-person-heart'),
(N'Phụ Kiện Thời Trang', N'Mũ, thắt lưng, túi xách, ví da, đồng hồ', 'bi-gem');
GO

-- Seed Users
INSERT INTO users (username, password, fullname, email, phone, address, role) VALUES
('admin', '123456', N'Quản Trị Viên HighFashion', 'admin@fashion.com', '0988776655', N'Hà Nội, Việt Nam', 1),
('customer1', '123456', N'Nguyễn Văn An', 'an.nguyen@gmail.com', '0912345678', N'123 Nguyễn Trãi, Quận 5, TP.HCM', 0);
GO

-- Seed Stores
INSERT INTO stores (name, address, city, phone, hours, latitude, longitude, is_main) VALUES
(N'HighFashion Flagship Sư Vạn Hạnh', N'561 Sư Vạn Hạnh, Phường Hòa Hưng, Quận 10', N'TP.HCM', '0901 234 567', '08:30 - 22:00', 10.7769, 106.6675, 1),
(N'HighFashion Nguyễn Trãi Store', N'123 Nguyễn Trãi, Phường 2, Quận 5', N'TP.HCM', '0902 345 678', '09:00 - 21:30', 10.7554, 106.6792, 0),
(N'HighFashion Lê Lợi Boutique', N'456 Lê Lợi, Phường Bến Thành, Quận 1', N'TP.HCM', '0903 456 789', '08:30 - 22:00', 10.7731, 106.6983, 0),
(N'HighFashion Hà Nội Tràng Tiền', N'24 Tràng Tiền, Quận Hoàn Kiếm', N'Hà Nội', '0904 567 890', '08:30 - 22:00', 21.0253, 105.8544, 0),
(N'HighFashion Cầu Giấy Premium', N'180 Cầu Giấy, Phường Quan Hoa, Quận Cầu Giấy', N'Hà Nội', '0905 678 901', '09:00 - 21:30', 21.0362, 105.7905, 0),
(N'HighFashion Đà Nẵng Nguyễn Văn Linh', N'88 Nguyễn Văn Linh, Quận Hải Châu', N'Đà Nẵng', '0906 789 012', '08:30 - 21:30', 16.0608, 108.2155, 0);
GO

-- Seed 24 San Pham
INSERT INTO products (name, description, price, sale_price, quantity, image, size, color, category_id, is_featured) VALUES
(N'Áo Sơ Mi Nam Oxford Premium', N'Áo sơ mi oxford cotton cao cấp thoáng mát, form dáng Slimfit trẻ trung.', 450000, 380000, 50, 'https://images.unsplash.com/photo-1602810318383-e386cc2a3ccf?w=600', 'S, M, L, XL', N'Trắng, Xanh Lam, Đen', 1, 1),
(N'Áo Thun Polo Nam Basic Cotton', N'Áo thun polo cá sấu 100% cotton co giãn 4 chiều, thấm hút mồ hôi tốt.', 290000, 0, 80, 'https://images.unsplash.com/photo-1625910513413-4ec2b1418f4a?w=600', 'M, L, XL, XXL', N'Đen, Trắng, Xanh Navy, Ghi', 1, 1),
(N'Áo Khoác Denim Jacket Classic', N'Áo khoác jeans bò cao cấp bụi bặm phong cách streetwear cá tính.', 650000, 0, 30, 'https://images.unsplash.com/photo-1576995853123-5a10305d93c0?w=600', 'M, L, XL', N'Xanh Navy, Đen', 1, 1),
(N'Áo Leather Biker Jacket Nam', N'Áo khoác da cừu cao cấp phong cách Biker thời thượng đẳng cấp.', 3200000, 0, 15, 'https://images.unsplash.com/photo-1551028719-00167b16eac5?w=600', 'S, M, L, XL', N'Đen, Nâu', 1, 1),
(N'Áo Măng Tô Dạ Nam High-End', N'Áo măng tô dạ dáng dài phong cách quý ông Châu Âu siêu ấm áp.', 4500000, 0, 10, 'https://images.unsplash.com/photo-1544441893-675973e31985?w=600', 'M, L, XL, XXL', N'Be, Ghi, Đen', 1, 0),
(N'Áo Hoodie Oversize Unisex Streetwear', N'Áo hoodie nỉ bông form rộng năng động trẻ trung cá tính.', 480000, 390000, 60, 'https://images.unsplash.com/photo-1556905055-8f358a7a47b2?w=600', 'XS, S, M, L, XL', N'Cam, Xanh Lá, Hồng, Vàng', 1, 1),
(N'Quần Jeans Nam Slimfit Co Giãn', N'Quần jeans nam chất liệu denim co giãn thoải mái, màu xanh trẻ trung.', 520000, 0, 45, 'https://images.unsplash.com/photo-1541099649105-f69ad21f3246?w=600', 'S, M, L, XL', N'Xanh Lam, Đen', 2, 1),
(N'Quần Tây Nam Hàn Quốc Uniqlo', N'Quần tây nam form ống đứng chuẩn phong cách Hàn Quốc chống nhăn.', 480000, 0, 60, 'https://images.unsplash.com/photo-1479064555552-3ef4979f8908?w=600', 'S, M, L, XL', N'Đen, Xám, Kem', 2, 0),
(N'Quần Short Cargo Nam Pocket', N'Quần short túi hộp cá tính chất liệu kaki dày dặn siêu bền.', 390000, 0, 50, 'https://images.unsplash.com/photo-1591195853828-11db59a44f6b?w=600', 'M, L, XL', N'Be, Xanh Lá, Đen', 2, 0),
(N'Quần Jogger Nam Fleece Thể Thao', N'Quần jogger thun nỉ co giãn đi chơi và tập luyện thể thao.', 490000, 0, 40, 'https://images.unsplash.com/photo-1552902865-b72c031ac5ea?w=600', 'S, M, L, XL', N'Ghi, Đen, Xanh Navy', 2, 0),
(N'Quần Trouser Da Cừu Luxury', N'Quần da cừu hàng hiệu may thủ công tinh tế dành cho doanh nhân.', 1950000, 0, 12, 'https://images.unsplash.com/photo-1624378439575-d8705ad7ae80?w=600', 'M, L, XL', N'Đen, Nâu', 2, 1),
(N'Quần Vải Linen Nam Mùa Hè', N'Quần đũi thoáng mát phong cách dạo biển lịch lãm.', 680000, 0, 35, 'https://images.unsplash.com/photo-1507679799987-c73779587ccf?w=600', 'S, M, L', N'Trắng, Kem, Be', 2, 0),
(N'Đầm Xòe Nữ Vintage Dáng Dài', N'Đầm nữ phong cách vintage nhẹ nhàng nữ tính, chất lụa satin mềm mịn.', 580000, 490000, 25, 'https://images.unsplash.com/photo-1539109136881-3be0616acf4b?w=600', 'XS, S, M, L', N'Trắng, Hồng, Kem', 3, 1),
(N'Áo Blazer Nữ Form Rộng Style Hàn', N'Áo blazer nữ thanh lịch hiện đại, phù hợp đi làm và dạo phố.', 720000, 0, 20, 'https://images.unsplash.com/photo-1584273143981-41c073dfe8f8?w=600', 'S, M, L', N'Be, Ghi, Đen', 3, 1),
(N'Váy Dạ Hội Sparkle Sequins Elegant', N'Váy dạ hội đính kim tuyến lấp lánh đi tiệc sang trọng kiêu sa.', 2800000, 0, 15, 'https://images.unsplash.com/photo-1566174053879-31528523f8ae?w=600', 'XS, S, M', N'Đỏ, Vàng, Đen', 3, 1),
(N'Váy Lụa Tơ Tằm Haute Couture Nữ', N'Thiết kế váy lụa tơ tằm may đo thủ công độc bản quyến rũ.', 5500000, 0, 8, 'https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?w=600', 'S, M', N'Đỏ, Trắng, Tím', 3, 0),
(N'Áo Croptop Nữ Dệt Kim Năng Động', N'Áo croptop len dệt kim ôm body khoe đường cong quyến rũ.', 220000, 0, 70, 'https://images.unsplash.com/photo-1503342217505-b0a15ec3261c?w=600', 'XS, S, M', N'Vàng, Cam, Xanh Lam', 3, 0),
(N'Set Trang Phục Vest Nữ Công Sở Luxe', N'Set áo vest và quần tây nữ sang trọng chuẩn style doanh nhân thành đạt.', 2100000, 0, 18, 'https://images.unsplash.com/photo-1487222477894-8943e31ef7b2?w=600', 'S, M, L', N'Trắng, Đen, Be', 3, 1),
(N'Túi Xách Da Nữ Crossbody Luxe', N'Túi xách da PU cao cấp khóa mạ vàng sang trọng ngăn chứa rộng rãi.', 390000, 320000, 40, 'https://images.unsplash.com/photo-1584917865442-de89df76afd3?w=600', 'Freesize', N'Đen, Nâu, Trắng', 4, 1),
(N'Thắt Lưng Da Bò Nam Mặt Kim Loại', N'Thắt lưng da bò thật 100% khóa kim loại chống gỉ tinh tế.', 390000, 0, 50, 'https://images.unsplash.com/photo-1624222247344-550fb60583dc?w=600', 'One Size', N'Đen, Nâu', 4, 0),
(N'Kính Mắt Thời Trang Unisex Anti-UV', N'Kính mát gọng kim loại chống tia UV400 bảo vệ mắt tối đa.', 550000, 0, 60, 'https://images.unsplash.com/photo-1511499767150-a48a237f0083?w=600', 'Freesize', N'Đen, Vàng, Vàng Vàng', 4, 0),
(N'Đồng Hồ Kim Nam Dây Da Thụy Sỹ', N'Đồng hồ nam máy Quartz chống nước dây da bò cao cấp lịch lãm.', 3500000, 0, 15, 'https://images.unsplash.com/photo-1522335789203-aabd1fc54bc9?w=600', 'One Size', N'Đen, Nâu, Vàng', 4, 1),
(N'Túi Du Lịch Canvas Unisex Vintage', N'Túi xách du lịch cỡ lớn vải canvas phối da mộc bụi bặm.', 1250000, 0, 25, 'https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=600', 'Freesize', N'Xanh Lá, Be, Nâu', 4, 0),
(N'Bộ Trang Sức Vòng Cổ Bạc Đính Đá', N'Bộ dây chuyền và bông tai bạc 925 đính đá pha sáng lấp lánh.', 7200000, 0, 10, 'https://images.unsplash.com/photo-1599643478518-a784e5dc4c8f?w=600', 'One Size', N'Trắng, Tím, Vàng', 4, 1);
GO
