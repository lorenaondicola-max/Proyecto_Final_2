-- =====================================================================
-- Sistema de turnos de canchas de tenis
-- Crea la base desde cero y carga los datos de prueba.
-- ATENCIÓN: borra la base existente con todos sus datos.
-- =====================================================================

SET NAMES utf8mb4;

DROP DATABASE IF EXISTS gestion_turnos_tenis_db;
CREATE DATABASE gestion_turnos_tenis_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE gestion_turnos_tenis_db;

-- ---------------------------------------------------------------------
-- Tablas
-- ---------------------------------------------------------------------

CREATE TABLE club (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    direccion VARCHAR(150) NOT NULL,
    hora_apertura TIME NOT NULL,
    hora_cierre TIME NOT NULL,
    inicio_hora_pico TIME NOT NULL,
    fin_hora_pico TIME NOT NULL,
    -- Días de la semana separados por coma, con los nombres de java.time.DayOfWeek
    dias_atencion VARCHAR(80) NOT NULL,
    horas_cancelacion INT NOT NULL,
    porcentaje_penalizacion DOUBLE NOT NULL,
    precio_base_hora DOUBLE NOT NULL,
    recargo_polvo_porcentaje DOUBLE NOT NULL,
    recargo_pico_porcentaje DOUBLE NOT NULL,
    recargo_dobles_porcentaje DOUBLE NOT NULL,
    descuento_socio_porcentaje DOUBLE NOT NULL
);

CREATE TABLE jugadores (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    dni VARCHAR(20) NOT NULL UNIQUE,
    fecha_nacimiento DATE NOT NULL,
    domicilio VARCHAR(150),
    telefono VARCHAR(30),
    email VARCHAR(100),
    username VARCHAR(50) UNIQUE,
    password_hash VARCHAR(255),
    es_socio BOOLEAN NOT NULL,
    nivel VARCHAR(30) NOT NULL,
    categoria VARCHAR(30),
    cancelaciones_tardias INT NOT NULL DEFAULT 0,
    responsable_id INT DEFAULT NULL,
    CONSTRAINT fk_jugador_responsable FOREIGN KEY (responsable_id)
        REFERENCES jugadores(id)
        ON DELETE SET NULL
        ON UPDATE CASCADE
);

-- Administradores y recepcionistas. El club solo se completa para los recepcionistas.
CREATE TABLE empleados (
    id INT AUTO_INCREMENT PRIMARY KEY,
    tipo VARCHAR(20) NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    dni VARCHAR(20) NOT NULL UNIQUE,
    fecha_nacimiento DATE NOT NULL,
    domicilio VARCHAR(150),
    telefono VARCHAR(30),
    email VARCHAR(100),
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    club_id INT DEFAULT NULL,
    CONSTRAINT fk_empleado_club FOREIGN KEY (club_id)
        REFERENCES club(id)
        ON DELETE SET NULL
        ON UPDATE CASCADE
);

CREATE TABLE canchas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    numero INT NOT NULL,
    superficie VARCHAR(30) NOT NULL,
    tiene_luz BOOLEAN NOT NULL,
    club_id INT NOT NULL,
    CONSTRAINT uq_cancha_numero UNIQUE (club_id, numero),
    CONSTRAINT fk_cancha_club FOREIGN KEY (club_id)
        REFERENCES club(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

CREATE TABLE bloqueos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    cancha_id INT NOT NULL,
    motivo VARCHAR(30) NOT NULL,
    desde DATETIME NOT NULL,
    hasta DATETIME NOT NULL,
    detalle VARCHAR(200),
    CONSTRAINT fk_bloqueo_cancha FOREIGN KEY (cancha_id)
        REFERENCES canchas(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

CREATE TABLE reservas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    cancha_id INT NOT NULL,
    jugador_id INT NOT NULL,
    titular_id INT DEFAULT NULL,
    inicio DATETIME NOT NULL,
    fin DATETIME NOT NULL,
    tipo_juego VARCHAR(30) NOT NULL,
    estado VARCHAR(30) NOT NULL,
    cargo_cancelacion DOUBLE NOT NULL,
    asistio BOOLEAN NOT NULL,
    CONSTRAINT fk_reserva_cancha FOREIGN KEY (cancha_id)
        REFERENCES canchas(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_reserva_jugador FOREIGN KEY (jugador_id)
        REFERENCES jugadores(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    CONSTRAINT fk_reserva_titular FOREIGN KEY (titular_id)
        REFERENCES jugadores(id)
        ON DELETE SET NULL
        ON UPDATE CASCADE
);

CREATE TABLE reserva_extras (
    id INT AUTO_INCREMENT PRIMARY KEY,
    reserva_id INT NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    precio_unitario DOUBLE NOT NULL,
    cantidad INT DEFAULT NULL,
    profesor VARCHAR(100) DEFAULT NULL,
    duracion_min INT DEFAULT NULL,
    CONSTRAINT fk_extra_reserva FOREIGN KEY (reserva_id)
        REFERENCES reservas(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

-- ---------------------------------------------------------------------
-- Datos de prueba
-- Las claves se guardan en texto plano: es una simplificación del trabajo.
-- ---------------------------------------------------------------------

INSERT INTO club (nombre, direccion, hora_apertura, hora_cierre, inicio_hora_pico, fin_hora_pico,
                  dias_atencion, horas_cancelacion, porcentaje_penalizacion,
                  precio_base_hora, recargo_polvo_porcentaje, recargo_pico_porcentaje,
                  recargo_dobles_porcentaje, descuento_socio_porcentaje)
VALUES
    ('Tenis Club Central', 'Av. Libertador 1234', '08:00', '22:00', '18:00', '21:00',
     'MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY,SATURDAY,SUNDAY', 24, 50.0,
     5000.0, 10.0, 20.0, 15.0, 20.0),
    ('Club Náutico Sur', 'Av. Costanera 500', '09:00', '21:00', '18:00', '20:00',
     'MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY,SATURDAY', 12, 30.0,
     6000.0, 10.0, 25.0, 15.0, 10.0);

-- Club 1: canchas 1 a 3. Club 2: canchas 1 y 2.
INSERT INTO canchas (numero, superficie, tiene_luz, club_id) VALUES
    (1, 'POLVO_LADRILLO', TRUE, 1),
    (2, 'CEMENTO', FALSE, 1),
    (3, 'CEMENTO', TRUE, 1),
    (1, 'POLVO_LADRILLO', TRUE, 2),
    (2, 'POLVO_LADRILLO', FALSE, 2);

-- Carlos (id 1) es el responsable de Lucas (id 3), que es menor y no tiene credenciales.
INSERT INTO jugadores (nombre, dni, fecha_nacimiento, domicilio, telefono, email, username,
                       password_hash, es_socio, nivel, categoria, responsable_id) VALUES
    ('Carlos Pérez', '30123456', '1985-05-12', 'Calle Falsa 123', '1122334455',
     'carlos@email.com', 'cperez', 'hash123', TRUE, 'AVANZADO', 'PRIMERA', NULL),
    ('Juan Estrugamou', '30886766', '1984-03-15', '9 de Julio 3111', '2235394052',
     'jmestrugamou@gmail.com', 'jmestrugamou', 'pass1234', FALSE, 'PRINCIPIANTE', 'SEXTA', NULL),
    ('Lucas Pérez', '45123456', '2010-08-20', 'Calle Falsa 123', '1155443322',
     'lucas@email.com', NULL, NULL, FALSE, 'INTERMEDIO', 'CUARTA', 1);

INSERT INTO empleados (tipo, nombre, dni, fecha_nacimiento, domicilio, telefono, email,
                       username, password_hash, club_id) VALUES
    ('ADMINISTRADOR', 'Marta Sosa', '20111222', '1975-02-10', 'Belgrano 900', '2234000000',
     'admin@tenis.com', 'admin', 'admin123', NULL),
    ('RECEPCIONISTA', 'Ana Gómez', '28111222', '1990-03-04', 'Calle Real 456', '1133221100',
     'ana@email.com', 'agomez', 'clave123', 1),
    ('RECEPCIONISTA', 'Pedro Ruiz', '29333444', '1988-07-21', 'Colón 2500', '2234111111',
     'pedro@email.com', 'pruiz', 'clave123', 2);
