/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.module.extension.internal.introspection.validation;

import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang.StringUtils.join;
import static org.mule.metadata.java.api.utils.JavaTypeUtils.getType;
import static org.mule.runtime.core.util.StringUtils.isBlank;
import static org.mule.runtime.extension.api.introspection.metadata.NullMetadataResolver.NULL_CATEGORY_NAME;
import static org.mule.runtime.module.extension.internal.util.IntrospectionUtils.getComponentModelTypeName;
import org.mule.metadata.api.model.DictionaryType;
import org.mule.metadata.api.model.ObjectType;
import org.mule.metadata.api.visitor.MetadataTypeVisitor;
import org.mule.runtime.api.metadata.resolving.InputTypeResolver;
import org.mule.runtime.api.metadata.resolving.NamedTypeResolver;
import org.mule.runtime.api.metadata.resolving.OutputTypeResolver;
import org.mule.runtime.extension.api.ExtensionWalker;
import org.mule.runtime.extension.api.exception.IllegalModelDefinitionException;
import org.mule.runtime.extension.api.introspection.ComponentModel;
import org.mule.runtime.extension.api.introspection.ExtensionModel;
import org.mule.runtime.extension.api.introspection.Named;
import org.mule.runtime.extension.api.introspection.RuntimeComponentModel;
import org.mule.runtime.extension.api.introspection.RuntimeExtensionModel;
import org.mule.runtime.extension.api.introspection.metadata.MetadataResolverFactory;
import org.mule.runtime.extension.api.introspection.metadata.NullMetadataResolver;
import org.mule.runtime.extension.api.introspection.operation.HasOperationModels;
import org.mule.runtime.extension.api.introspection.operation.OperationModel;
import org.mule.runtime.extension.api.introspection.parameter.ParameterModel;
import org.mule.runtime.extension.api.introspection.property.MetadataKeyIdModelProperty;
import org.mule.runtime.extension.api.introspection.property.MetadataKeyPartModelProperty;
import org.mule.runtime.extension.api.introspection.source.HasSourceModels;
import org.mule.runtime.extension.api.introspection.source.SourceModel;

import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Validates that all {@link OperationModel operations} which return type is a {@link Object} or a {@link Map} have defined a
 * {@link OutputTypeResolver}. The {@link OutputTypeResolver} can't be the {@link NullMetadataResolver}.
 *
 * @since 4.0
 */
public class MetadataComponentModelValidator implements ModelValidator {

  @Override
  public void validate(ExtensionModel extensionModel) throws IllegalModelDefinitionException {
    if (!(extensionModel instanceof RuntimeExtensionModel)) {
      return;
    }
    new ExtensionWalker() {

      @Override
      public void onOperation(HasOperationModels owner, OperationModel model) {
        validateComponent(model);
      }

      @Override
      public void onSource(HasSourceModels owner, SourceModel model) {
        validateComponent(model);
      }

      private void validateComponent(ComponentModel model) {
        validateMetadataReturnType(extensionModel, model);
        validateMetadataReturnType(extensionModel, model);
        MetadataResolverFactory resolverFactory = ((RuntimeComponentModel) model).getMetadataResolverFactory();
        validateMetadataKeyId(model, resolverFactory);
        validateCategoriesInScope(model, resolverFactory);
      }
    }.walk(extensionModel);
  }

  private void validateMetadataKeyId(ComponentModel model, MetadataResolverFactory resolverFactory) {
    Optional<MetadataKeyIdModelProperty> keyId = model.getModelProperty(MetadataKeyIdModelProperty.class);
    if (keyId.isPresent()) {

      if (resolverFactory.getOutputResolver() instanceof NullMetadataResolver &&
        getAllInputResolvers(model, resolverFactory).isEmpty()) {

        throw new IllegalModelDefinitionException(format("Component [%s] defines a MetadataKeyId parameter but neither"
                                                           + " an Output nor Type resolver that makes use of it was defined",
                                                         model.getName()));
      }

      keyId.get().getType().accept(new MetadataTypeVisitor() {

        public void visitObject(ObjectType objectType) {
          List<ParameterModel> parts = model.getParameterModels().stream()
            .filter(p -> p.getModelProperty(MetadataKeyPartModelProperty.class).isPresent()).collect(toList());

          List<ParameterModel> defaultParts = parts.stream().filter(p -> p.getDefaultValue() != null).collect(toList());

          if (!defaultParts.isEmpty() && defaultParts.size() != parts.size()) {
            throw new IllegalModelDefinitionException(
              format("[%s] type multilevel key defines [%s] MetadataKeyPart with default values, but the type contains [%s] "
                       + "MetadataKeyParts. All the annotated MetadataKeyParts should have a default value if at least one part "
                       + "has a default value.", getType(objectType).getSimpleName(),
                     defaultParts.size(), parts.size()));
          }
        }
      });
    } else {
      if (!(resolverFactory.getKeyResolver() instanceof NullMetadataResolver)) {
        throw new IllegalModelDefinitionException(format("Component [%s] does not define a MetadataKeyId parameter but "
                                                           + "a type keys resolver of type [%s] was associated to it",
                                                         model.getName(), resolverFactory.getKeyResolver().getClass().getName()));
      }
    }

  }

  private void validateMetadataReturnType(ExtensionModel extensionModel, ComponentModel component) {
    component.getOutput().getType().accept(new MetadataTypeVisitor() {

      @Override public void visitObject(ObjectType objectType) {
        validateReturnType(extensionModel, (RuntimeComponentModel) component, getType(objectType));
      }

      @Override public void visitDictionary(DictionaryType dictionaryType) {
        validateReturnType(extensionModel, (RuntimeComponentModel) component, getType(dictionaryType.getValueType()));
      }
    });
  }

  private void validateReturnType(ExtensionModel extensionModel, RuntimeComponentModel component, Class<?> returnType) {
    if (Object.class.equals(returnType)
      && component.getMetadataResolverFactory().getOutputResolver() instanceof NullMetadataResolver) {
      throw new IllegalModelDefinitionException(format("%s '%s' specifies '%s' as a return type. Operations and Sources with "
                                                         + "return type such as Object or Map must have defined a not null OutputTypeResolver",
                                                       component.getName(),
                                                       extensionModel.getName(), returnType.getName()));
    }
  }

  private void validateCategoriesInScope(ComponentModel componentModel, MetadataResolverFactory metadataResolverFactory) {

    ImmutableList.Builder<NamedTypeResolver> resolvers = ImmutableList.<NamedTypeResolver>builder()
      .add(metadataResolverFactory.getKeyResolver())
      .add(metadataResolverFactory.getOutputResolver())
      .addAll(getAllInputResolvers(componentModel, metadataResolverFactory));

    validateCategoryNames(componentModel, resolvers.build().toArray(new NamedTypeResolver[] {}));
  }

  private List<InputTypeResolver<Object>> getAllInputResolvers(ComponentModel componentModel,
                                                               MetadataResolverFactory resolverFactory) {
    return componentModel.getParameterModels().stream().map(Named::getName)
      .map(resolverFactory::getInputResolver).collect(toList());
  }

  private void validateCategoryNames(ComponentModel componentModel, NamedTypeResolver... resolvers) {
    stream(resolvers).filter(r -> isBlank(r.getCategoryName()))
      .findFirst().ifPresent(r -> {
      throw new IllegalModelDefinitionException(
        format("%s '%s' specifies a metadata resolver [%s] which has an empty category name",
               getComponentModelTypeName(componentModel), componentModel.getName(),
               r.getClass().getSimpleName()));
    });

    Set<String> names = stream(resolvers)
      .map(NamedTypeResolver::getCategoryName)
      .filter(r -> !r.equals(NULL_CATEGORY_NAME))
      .collect(toSet());

    if (names.size() > 1) {
      throw new IllegalModelDefinitionException(format(
        "%s '%s' specifies metadata resolvers that doesn't belong to the same category. The following categories were the ones found [%s]",
        getComponentModelTypeName(componentModel), componentModel.getName(),
        join(names, ",")));
    }
  }
}
