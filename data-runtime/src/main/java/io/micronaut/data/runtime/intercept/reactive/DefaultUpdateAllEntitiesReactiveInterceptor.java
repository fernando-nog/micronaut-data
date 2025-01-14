/*
 * Copyright 2017-2020 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.data.runtime.intercept.reactive;

import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.core.type.Argument;
import io.micronaut.core.type.ReturnType;
import io.micronaut.data.intercept.RepositoryMethodKey;
import io.micronaut.data.intercept.reactive.UpdateAllEntitiesReactiveInterceptor;
import io.micronaut.data.operations.RepositoryOperations;
import org.reactivestreams.Publisher;

/**
 * Default implementation of {@link UpdateAllEntitiesReactiveInterceptor}.
 * @param <T> The declaring type
 * @param <R> The return type
 * @author Denis Stepanov
 * @since 2.4.0
 */
public class DefaultUpdateAllEntitiesReactiveInterceptor<T, R> extends AbstractReactiveInterceptor<T, R>
        implements UpdateAllEntitiesReactiveInterceptor<T, R> {

    /**
     * Default constructor.
     * @param operations The operations
     */
    public DefaultUpdateAllEntitiesReactiveInterceptor(@NonNull RepositoryOperations operations) {
        super(operations);
    }

    @Override
    public R intercept(RepositoryMethodKey methodKey, MethodInvocationContext<T, R> context) {
        Iterable<R> iterable = (Iterable<R>) getEntitiesParameter(context, Object.class);
        //noinspection unchecked
        Class<R> rootEntity = (Class<R>) getRequiredRootEntity(context);
        Publisher<R> rs = reactiveOperations.updateAll(getUpdateAllBatchOperation(context, rootEntity, iterable));
        ReturnType<R> rt = context.getReturnType();
        Argument<?> reactiveValue = context.getReturnType().asArgument().getFirstTypeVariable().orElse(Argument.OBJECT_ARGUMENT);
        if (isNumber(reactiveValue.getType())) {
            return operations.getConversionService().convert(count(rs), rt.asArgument())
                        .orElseThrow(() -> new IllegalStateException("Unsupported return type: " + rt.getType()));
        }
        return Publishers.convertPublisher(rs, context.getReturnType().getType());
    }
}
