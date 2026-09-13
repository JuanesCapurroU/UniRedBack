-- Solo un super administrador puede promover a otros administradores.
-- El administrador mas antiguo queda como super admin; el resto sigue como ADMIN normal.
UPDATE usuarios SET nivel_acceso = 'ADMIN'
 WHERE dtype = 'ADMINISTRADOR' AND (nivel_acceso IS NULL OR nivel_acceso <> 'SUPER_ADMIN');

UPDATE usuarios SET nivel_acceso = 'SUPER_ADMIN'
 WHERE id = (SELECT id FROM usuarios WHERE dtype = 'ADMINISTRADOR' ORDER BY id LIMIT 1);
