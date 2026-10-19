package com.tripsi.api.shared.logging;

import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class CorrelationIdFilterTest {

    private final CorrelationIdFilter filter = new CorrelationIdFilter();

    @Test
    void should_generate_correlation_id_and_echo_it_in_response_header_when_header_absent() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        String[] mdcValueDuringChain = new String[1];

        filter.doFilter(request, response, (req, res) -> mdcValueDuringChain[0] = MDC.get(CorrelationIdFilter.MDC_KEY));

        assertThat(mdcValueDuringChain[0]).isNotBlank();
        assertThat(response.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER)).isEqualTo(mdcValueDuringChain[0]);
    }

    @Test
    void should_reuse_incoming_correlation_id_when_header_present() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(CorrelationIdFilter.CORRELATION_ID_HEADER, "existing-id-123");
        MockHttpServletResponse response = new MockHttpServletResponse();
        String[] mdcValueDuringChain = new String[1];

        filter.doFilter(request, response, (req, res) -> mdcValueDuringChain[0] = MDC.get(CorrelationIdFilter.MDC_KEY));

        assertThat(mdcValueDuringChain[0]).isEqualTo("existing-id-123");
        assertThat(response.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER)).isEqualTo("existing-id-123");
    }

    @Test
    void should_clear_mdc_after_request_completes() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (req, res) -> { });

        assertThat(MDC.get(CorrelationIdFilter.MDC_KEY)).isNull();
    }
}
