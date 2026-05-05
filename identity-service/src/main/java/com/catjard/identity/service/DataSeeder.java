package com.catjard.identity.service;

import com.catjard.identity.model.Rol;
import com.catjard.identity.model.Usuario;
import com.catjard.identity.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UsuarioRepository repo;
    private final PasswordEncoder encoder;

    @Override
    public void run(String... args) {
        if (repo.count() > 0) {
            log.info("DataSeeder: usuarios ya existen ({}), no se siembra.", repo.count());
            return;
        }
        log.info("DataSeeder: tabla vacia, sembrando 5 usuarios mock...");
        var seeds = List.of(
                Usuario.builder()
                        .email("cliente@empresa.com")
                        .password(encoder.encode("cliente123"))
                        .rol(Rol.cliente)
                        .nombre("Lucia Montoya")
                        .empresa("Banco Sigma")
                        .ruc("20512345671")
                        .telefono("+51 999 111 222")
                        .direccion("Av. Javier Prado 1234, San Isidro, Lima")
                        .build(),
                Usuario.builder()
                        .email("vendedor@catjard.pe")
                        .password(encoder.encode("vendedor123"))
                        .rol(Rol.vendedor)
                        .nombre("Carlos Rivas")
                        .cargo("Ejecutivo de cuentas")
                        .build(),
                Usuario.builder()
                        .email("almacen@catjard.pe")
                        .password(encoder.encode("almacen123"))
                        .rol(Rol.almacen)
                        .nombre("Marta Salinas")
                        .cargo("Jefe de Almacen")
                        .build(),
                Usuario.builder()
                        .email("produccion@catjard.pe")
                        .password(encoder.encode("produccion123"))
                        .rol(Rol.produccion)
                        .nombre("Diego Flores")
                        .cargo("Jefe de Produccion")
                        .build(),
                Usuario.builder()
                        .email("gerente@catjard.pe")
                        .password(encoder.encode("gerente123"))
                        .rol(Rol.gerente)
                        .nombre("Ana Reategui")
                        .cargo("Gerente General")
                        .build()
        );
        repo.saveAll(seeds);
        log.info("DataSeeder: {} usuarios creados.", seeds.size());
    }
}
