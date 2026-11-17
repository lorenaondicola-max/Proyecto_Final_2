DROP DATABASE IF EXISTS gestion_turnos_tenis_db;
CREATE DATABASE gestion_turnos_tenis_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE gestion_turnos_tenis_db;

CREATE TABLE club (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    direccion VARCHAR(150) NOT NULL,
    hora_apertura TIME NOT NULL,
    hora_cierre TIME NOT NULL,
    inicio_hora_pico TIME NOT NULL,
    fin_hora_pico TIME NOT NULL,
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
    username VARCHAR(50),
    password_hash VARCHAR(255),
    es_socio BOOLEAN NOT NULL,
    nivel VARCHAR(30) NOT NULL, 
    categoria VARCHAR(30),
    responsable_id INT DEFAULT NULL,
    CONSTRAINT fk_jugador_responsable FOREIGN KEY (responsable_id) 
        REFERENCES jugadores(id) 
        ON DELETE SET NULL 
        ON UPDATE CASCADE
);

CREATE TABLE canchas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    numero INT NOT NULL,
    superficie VARCHAR(30) NOT NULL, 
    tiene_luz BOOLEAN NOT NULL,
    club_id INT NOT NULL,
    CONSTRAINT fk_cancha_club FOREIGN KEY (club_id) 
        REFERENCES club(id) 
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