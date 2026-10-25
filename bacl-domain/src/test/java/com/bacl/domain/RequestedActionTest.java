package com.bacl.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class RequestedActionTest {

    @Test
    void transferRequiresPayeeAndAmount() {
        assertThatThrownBy(() -> new RequestedAction("ACT-1", ActionType.TRANSFER, Field.customerStated("A1"),
                Optional.empty(), Optional.of(Field.customerStated(new BigDecimal("100.00"))), Map.of(), 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("payee");
    }

    @Test
    void amountMustBePositive() {
        assertThatThrownBy(() -> RequestedAction.moneyMovement("ACT-1", ActionType.PAYMENT,
                Field.customerStated("A1"), Field.customerStated("P1"), Field.customerStated(BigDecimal.ZERO), 1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void loanModificationNeedsNoPayeeOrAmount() {
        RequestedAction action = RequestedAction.withParams("ACT-2", ActionType.LOAN_MODIFICATION,
                Field.customerStated("A1"), Map.of("loanId", Field.customerStated("L1")), 3);
        assertThat(action.payee()).isEmpty();
        assertThat(action.amount()).isEmpty();
    }

    @Test
    void turnsStartAtOne() {
        assertThatThrownBy(() -> RequestedAction.withParams("ACT-3", ActionType.ACCOUNT_CHANGE,
                Field.customerStated("A1"), Map.of(), 0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void adr005_allFieldsExposesProvenanceOfEveryField() {
        // "Send the usual to the account in this email": amount inferred, payee from external content.
        RequestedAction action = RequestedAction.moneyMovement("ACT-4", ActionType.PAYMENT,
                Field.customerStated("A1"), Field.externalContent("12345"),
                Field.agentInferred(new BigDecimal("2000.00")), 2);
        assertThat(action.allFields().map(Field::provenance))
                .containsExactly(Provenance.CUSTOMER_STATED, Provenance.EXTERNAL_CONTENT, Provenance.AGENT_INFERRED);
    }
}
