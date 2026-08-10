-- Los usuarios existentes se consideran verificados para no interrumpir sus cuentas.
-- Los registros nuevos quedan inactivos hasta confirmar el enlace enviado por correo.
ALTER TABLE usuarios
    ADD COLUMN email_verificado BOOLEAN NOT NULL DEFAULT TRUE AFTER activo;
