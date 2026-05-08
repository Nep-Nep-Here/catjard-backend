package com.catjard.sales.service;

import com.catjard.sales.dto.ItemDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitarios para TotalesCalculator.
 * Logica pura: subtotal = sum(precioUnit * cantidad), IGV = 18% subtotal,
 * total = subtotal + IGV. Todo redondeado a 2 decimales con HALF_UP.
 */
class TotalesCalculatorTest {

    @Test
    @DisplayName("un solo item: subtotal = precioUnit * cantidad, IGV 18%, total = subtotal + IGV")
    void unSoloItem() {
        var items = List.of(
                new ItemDTO(1L, 100, new BigDecimal("10.00"), "Serigrafia", null)
        );

        var t = TotalesCalculator.calcular(items);

        assertThat(t.subtotal()).isEqualByComparingTo("1000.00");
        assertThat(t.igv()).isEqualByComparingTo("180.00");
        assertThat(t.total()).isEqualByComparingTo("1180.00");
    }

    @Test
    @DisplayName("varios items: suma todos los precioUnit*cantidad antes de aplicar IGV")
    void variosItems() {
        // 300 x 18.70 = 5610.00
        // 500 x 4.68  = 2340.00
        // subtotal = 7950.00, IGV = 1431.00, total = 9381.00
        var items = List.of(
                new ItemDTO(1L, 300, new BigDecimal("18.70"), "Grabado Laser", null),
                new ItemDTO(2L, 500, new BigDecimal("4.68"), "Grabado Laser", null)
        );

        var t = TotalesCalculator.calcular(items);

        assertThat(t.subtotal()).isEqualByComparingTo("7950.00");
        assertThat(t.igv()).isEqualByComparingTo("1431.00");
        assertThat(t.total()).isEqualByComparingTo("9381.00");
    }

    @Test
    @DisplayName("lista vacia: subtotal, IGV y total son cero")
    void listaVacia() {
        var t = TotalesCalculator.calcular(List.of());

        assertThat(t.subtotal()).isEqualByComparingTo("0.00");
        assertThat(t.igv()).isEqualByComparingTo("0.00");
        assertThat(t.total()).isEqualByComparingTo("0.00");
    }

    @Test
    @DisplayName("redondeo: 2 decimales con HALF_UP en subtotal e IGV")
    void redondeoMediaArriba() {
        // 33.33 * 3 = 99.99
        // IGV = 99.99 * 0.18 = 17.9982 -> redondea a 18.00
        // total = 99.99 + 18.00 = 117.99
        var items = List.of(
                new ItemDTO(1L, 3, new BigDecimal("33.33"), null, null)
        );

        var t = TotalesCalculator.calcular(items);

        assertThat(t.subtotal()).isEqualByComparingTo("99.99");
        assertThat(t.igv()).isEqualByComparingTo("18.00");
        assertThat(t.total()).isEqualByComparingTo("117.99");
    }

    @Test
    @DisplayName("IGV es exactamente 18% del subtotal")
    void igvEs18Porciento() {
        var items = List.of(
                new ItemDTO(1L, 1, new BigDecimal("100.00"), null, null)
        );

        var t = TotalesCalculator.calcular(items);

        assertThat(t.igv()).isEqualByComparingTo("18.00");
        assertThat(t.total()).isEqualByComparingTo("118.00");
    }
}
