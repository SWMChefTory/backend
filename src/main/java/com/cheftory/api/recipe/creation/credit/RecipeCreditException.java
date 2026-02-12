package com.cheftory.api.recipe.creation.credit;

import com.cheftory.api.credit.exception.CreditException;
import com.cheftory.api.exception.Error;

public class RecipeCreditException extends CreditException {
    public RecipeCreditException(Error error) {
        super(error);
    }
}
