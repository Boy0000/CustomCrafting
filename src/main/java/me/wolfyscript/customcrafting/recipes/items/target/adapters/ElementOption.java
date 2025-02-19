/*
 *       ____ _  _ ____ ___ ____ _  _ ____ ____ ____ ____ ___ _ _  _ ____
 *       |    |  | [__   |  |  | |\/| |    |__/ |__| |___  |  | |\ | | __
 *       |___ |__| ___]  |  |__| |  | |___ |  \ |  | |     |  | | \| |__]
 *
 *       CustomCrafting Recipe creation and management tool for Minecraft
 *                      Copyright (C) 2021  WolfyScript
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.wolfyscript.customcrafting.recipes.items.target.adapters;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import me.wolfyscript.lib.com.fasterxml.jackson.annotation.JsonGetter;
import me.wolfyscript.lib.com.fasterxml.jackson.annotation.JsonInclude;
import me.wolfyscript.utilities.util.eval.context.EvalContext;
import me.wolfyscript.utilities.util.eval.operators.BoolOperator;
import me.wolfyscript.utilities.util.eval.value_providers.ValueProvider;

public abstract class ElementOption<O, C> {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private ValueProvider<Integer> index;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private BoolOperator condition;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private ValueProvider<C> value;

    public Optional<ValueProvider<Integer>> index() {
        return Optional.ofNullable(index);
    }

    @JsonGetter
    ValueProvider<Integer> getIndex() {
        return index;
    }

    public void setIndex(ValueProvider<Integer> index) {
        this.index = index;
    }

    public Optional<BoolOperator> condition() {
        return Optional.ofNullable(condition);
    }

    public void setCondition(BoolOperator condition) {
        this.condition = condition;
    }

    @JsonGetter
    public BoolOperator getCondition() {
        return condition;
    }

    public Optional<ValueProvider<C>> value() {
        return Optional.ofNullable(value);
    }

    public void setValue(ValueProvider<C> value) {
        this.value = value;
    }

    public abstract boolean isEqual(O value, EvalContext evalContext);

    public List<O> readFromSource(List<O> source, EvalContext evalContext) {
        List<O> result = new ArrayList<>();
        if (condition().map(boolOperator -> boolOperator.evaluate(evalContext)).orElse(true)) {
            index().ifPresentOrElse(indexProvider -> {
                int index = indexProvider.getValue(evalContext);
                if (index < 0) {
                    index = source.size() + (index % source.size()); //Convert the negative index to a positive reverted index, that starts from the end.
                }
                index = index % source.size(); //Prevent out of bounds
                if (source.size() > index) {
                    O targetValue = source.get(index);
                    value().ifPresentOrElse(valueProvider -> {
                        if (isEqual(targetValue, evalContext)) {
                            result.add(targetValue);
                        }
                    }, () -> result.add(targetValue));
                }
            }, () -> value().ifPresentOrElse(valueProvider -> {
                for (O targetValue : source) {
                    if (isEqual(targetValue, evalContext)) {
                        result.add(targetValue);
                    }
                }
            }, () -> result.addAll(source)));
        }
        return result;
    }

    @JsonGetter
    protected ValueProvider<C> getValue() {
        return value;
    }

}

