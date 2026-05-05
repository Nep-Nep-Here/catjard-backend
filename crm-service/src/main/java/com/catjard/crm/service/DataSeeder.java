package com.catjard.crm.service;

import com.catjard.crm.model.ClienteCRM;
import com.catjard.crm.model.EstadoLead;
import com.catjard.crm.model.Lead;
import com.catjard.crm.repository.ClienteCRMRepository;
import com.catjard.crm.repository.LeadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final LeadRepository leadRepo;
    private final ClienteCRMRepository clienteRepo;

    @Override
    public void run(String... args) {
        if (leadRepo.count() == 0) seedLeads();
        if (clienteRepo.count() == 0) seedClientes();
    }

    private void seedLeads() {
        List<Lead> leads = List.of(
                Lead.builder()
                        .codigo("LEAD-2026-0034").fecha(LocalDate.of(2026, 4, 30))
                        .nombre("Andrea Vargas").empresa("Pacifico Salud").ruc("20517612345")
                        .email("andrea@pacifico.pe").telefono("+51 998 555 333")
                        .productos("Polos, libretas, lapiceros").cantidad("300")
                        .mensaje("Necesitamos kits de bienvenida para 300 nuevos colaboradores. Plazo: 3 semanas.")
                        .estado(EstadoLead.nuevo).build(),
                Lead.builder()
                        .codigo("LEAD-2026-0033").fecha(LocalDate.of(2026, 4, 29))
                        .nombre("Renzo Castillo").empresa("Globant Peru").ruc("20498765432")
                        .email("renzo.castillo@globant.com").telefono("+51 977 222 111")
                        .productos("Hoodies premium").cantidad("120")
                        .mensaje("Hoodies para evento de fin de anio, necesitamos calidad alta. Diseno minimalista.")
                        .estado(EstadoLead.contactado).asignadoA("Carlos Rivas")
                        .notasInternas("Llamada el 30/04. Pidio muestras fisicas, se las llevamos el viernes.")
                        .build(),
                Lead.builder()
                        .codigo("LEAD-2026-0032").fecha(LocalDate.of(2026, 4, 27))
                        .nombre("Maria Salazar").empresa("Inkaferry Tours").ruc("20611223344")
                        .email("msalazar@inkaferry.com.pe").telefono("+51 955 888 666")
                        .productos("Mochilas, gorras, tomatodos").cantidad("500")
                        .mensaje("Para tripulacion nueva temporada. Diseno con marca corporativa.")
                        .estado(EstadoLead.convertido).asignadoA("Carlos Rivas")
                        .notasInternas("Cotizacion COT-2026-0061 enviada y aprobada.")
                        .build(),
                Lead.builder()
                        .codigo("LEAD-2026-0031").fecha(LocalDate.of(2026, 4, 26))
                        .nombre("Javier Mendoza").empresa("EmpresaXYZ")
                        .email("javier.mendoza@gmail.com").telefono("+51 911 222 333")
                        .productos("Tazas").cantidad("20")
                        .mensaje("Necesito 20 tazas con logo.")
                        .estado(EstadoLead.descartado).asignadoA("Carlos Rivas")
                        .notasInternas("No cumple pedido minimo (50 und). Se le derivo a competencia.")
                        .build(),
                Lead.builder()
                        .codigo("LEAD-2026-0030").fecha(LocalDate.of(2026, 4, 25))
                        .nombre("Patricia Jimenez").empresa("Universidad Continental").ruc("20389123456")
                        .email("p.jimenez@continental.edu.pe").telefono("+51 944 777 222")
                        .productos("Agendas 2027 y libretas").cantidad("800")
                        .mensaje("Para entrega a docentes en ceremonia institucional. Diseno coordinado con identidad UCI.")
                        .estado(EstadoLead.nuevo).build()
        );
        leadRepo.saveAll(leads);
    }

    private void seedClientes() {
        List<ClienteCRM> clientes = List.of(
                cli("Banco Sigma S.A.A.", "Banco Sigma", "20512345671", "Banca",
                        "Lucia Montoya", "cliente@empresa.com", "+51 999 111 222",
                        "Av. Javier Prado 1234, San Isidro, Lima", true,
                        LocalDate.of(2025, 6, 15),
                        "Cliente recurrente. Cierra eventos grandes en Q4."),
                cli("Crehana Education Inc.", "Crehana", "20498123456", "EdTech",
                        "Diego Bracamonte", "diego.bracamonte@crehana.com", "+51 977 333 555",
                        "Av. Las Begonias 415, San Isidro, Lima", false,
                        LocalDate.of(2025, 8, 20),
                        "Pedidos de kits de bienvenida cada trimestre."),
                cli("BCP Wealth Management S.A.", "BCP Wealth", "20100012345", "Banca privada",
                        "Ana Reategui", "ana.reategui@bcpwealth.pe", "+51 988 444 222",
                        "Calle Las Camelias 700, San Isidro, Lima", false,
                        LocalDate.of(2024, 11, 10),
                        "Eventos VIP. Acabados premium."),
                cli("AJE Group Peru S.A.C.", "AJE Group", "20330456789", "Bebidas",
                        "Fernando Castro", "fcastro@ajegroup.com", "+51 966 555 444",
                        "Av. La Paz 1314, Ate, Lima", false,
                        LocalDate.of(2025, 2, 1),
                        "Pedidos grandes (2k+ unidades)."),
                cli("Pacifico Compania de Seguros y Reaseguros", "Pacifico Seguros", "20100040218", "Seguros",
                        "Carolina Nunez", "cnunez@pacifico.com.pe", "+51 922 333 444",
                        "Av. Juan de Arona 830, San Isidro, Lima", false,
                        LocalDate.of(2024, 9, 12),
                        "Renueva uniformes cada anio en marzo."),
                cli("Rappi Peru S.A.C.", "Rappi Peru", "20602445566", "Logistica",
                        "Mateo Espinoza", "mateo.espinoza@rappi.com", "+51 933 222 111",
                        "Calle Las Begonias 475, San Isidro, Lima", false,
                        LocalDate.of(2025, 1, 30),
                        "Tirajes muy altos, necesita rapidez.")
        );
        clienteRepo.saveAll(clientes);
    }

    private ClienteCRM cli(String razon, String nomCom, String ruc, String industria,
                           String contacto, String email, String tel, String direccion,
                           boolean activa, LocalDate alta, String notas) {
        return ClienteCRM.builder()
                .razonSocial(razon).nombreComercial(nomCom).ruc(ruc).industria(industria)
                .contactoPrincipal(contacto).email(email).telefono(tel).direccion(direccion)
                .cuentaActiva(activa).fechaAlta(alta).notas(notas)
                .build();
    }
}
