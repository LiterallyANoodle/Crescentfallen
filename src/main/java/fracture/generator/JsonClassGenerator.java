package fracture.generator;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

import com.google.auto.service.AutoService;

import fracture.annotation.GeneratorTarget;
import fracture.annotation.Literal;
import fracture.annotation.Template;

@AutoService(Processor.class)
public class JsonClassGenerator extends AbstractProcessor {
	
	private Types typeUtils;
	private Elements elementUtils;
	private Filer filer;
	private Messager messager; 
	private Map<String, String> factoryClasses = new LinkedHashMap<String, String>();
	
	@Override
	public synchronized void init(ProcessingEnvironment env) {
		super.init(env);
		typeUtils = env.getTypeUtils();
		elementUtils = env.getElementUtils();
		filer = env.getFiler();
		messager = env.getMessager();
	}
	
	@Override
	public Set<String> getSupportedAnnotationTypes() {
		Set<String> annotations = new LinkedHashSet<String>();
		annotations.add(GeneratorTarget.class.getCanonicalName());
		annotations.add(Literal.class.getCanonicalName());
		annotations.add(Template.class.getCanonicalName());
		return annotations;		
	}
	
	@Override
	public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.latestSupported();
	}
	
	@Override 
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		
		// errored is WRITE ONLY in the validation checks. It should never be READ to be used in control flow during validation.
		// AFTER validation, errored can be read to see if it is safe to generate the classes. 
		boolean errored = false;
		
		for (Element generatorTargetElement : roundEnv.getElementsAnnotatedWith(GeneratorTarget.class)) {
			
			GeneratorTarget annotation = generatorTargetElement.getAnnotation(GeneratorTarget.class);
			String packageLocation = annotation.packageLocation();
			String generatedClassName = annotation.generatedClassName();
			
			// packageLocation validation
			if ("".equals(packageLocation)) {
				messager.printMessage(
						Diagnostic.Kind.NOTE, 
						"@GeneratorTarget has no packageLocation - Generated class will be placed in the root package", 
						generatorTargetElement
				);
			} else {
				String[] packagePaths = packageLocation.split("\\.");
				boolean badPath = false;
				for (int i = 0; i < packagePaths.length; i++) {
					if (!isValidJavaIdentifier(packagePaths[i])) {
						messager.printMessage(
								Diagnostic.Kind.ERROR, 
								"@GeneratorTarget requires packageLocation to be a valid package name", 
								generatorTargetElement
						);
						badPath = true;
						break;
					}
				}
				// badPath is used locally to break the loop without reading from errored (see comment above errored definition)
				if (badPath) {
					errored = true;
					continue;
				}
			}
			
			// generatedClassName validation
			if ("".equals(generatedClassName)) {
				messager.printMessage(
						Diagnostic.Kind.ERROR, 
						"@GeneratorTarget requires generatedClassName to be defined", 
						generatorTargetElement
				);
				errored = true;
				continue;
			} else if (!isValidJavaIdentifier(generatedClassName)) {
				messager.printMessage(
						Diagnostic.Kind.ERROR, 
						"@GeneratorTarget requires generatedClassName to be a valid class name", 
						generatorTargetElement
				);
				errored = true;
				continue;
			}
			
		}
		
		for (Element literalElement : roundEnv.getElementsAnnotatedWith(Literal.class)) {
			
			// Check that the @Literal annotation is inside of a type with the @GeneratorTarget annotation.
			Element enclosing = literalElement.getEnclosingElement();
			if (null == enclosing.getAnnotation(GeneratorTarget.class)) {
				messager.printMessage(
						Diagnostic.Kind.ERROR, 
						"@Literal can only be used inside a type annotated with @GeneratorTarget", 
						literalElement
				);
				errored = true;
				continue;
			}
			
		}
		
		for (Element templateElement : roundEnv.getElementsAnnotatedWith(Template.class)) {
			
			ExecutableElement templateMethod = (ExecutableElement) templateElement;
			
			// Check that the @Template annotation is inside of a type with the @GeneratorTarget annotation.
			Element enclosing = templateElement.getEnclosingElement();
			if (null == enclosing.getAnnotation(GeneratorTarget.class)) {
				messager.printMessage(
						Diagnostic.Kind.ERROR, 
						"@Template can only be used inside a type annotated with @GeneratorTarget", 
						templateElement
				);
				errored = true;
				continue;
			}
			
			if (!templateMethod.getModifiers().contains(Modifier.PUBLIC)) {
				messager.printMessage(
						Diagnostic.Kind.ERROR, 
						"@Template methods must be public", 
						templateElement
				);
				errored = true;
				continue;
			}
			
			if (!templateMethod.getModifiers().contains(Modifier.STATIC)) {
				messager.printMessage(
						Diagnostic.Kind.ERROR, 
						"@Template methods must be static", 
						templateElement
				);
				errored = true;
				continue;
			}
			
			TypeMirror returnType = templateMethod.getReturnType();
			boolean validReturn = typeUtils.isSameType(returnType, elementUtils.getTypeElement("com.squareup.javapoet.FieldSpec").asType())
				    || typeUtils.isSameType(returnType, elementUtils.getTypeElement("com.squareup.javapoet.MethodSpec").asType())
				    || typeUtils.isSameType(returnType, elementUtils.getTypeElement("com.squareup.javapoet.TypeSpec").asType());
			if (!validReturn) {
				messager.printMessage(
						Diagnostic.Kind.ERROR, 
						"@Template must return a FieldSpec, MethodSpec, or TypeSpec", 
						templateElement
				);
				errored = true;
				continue;
			}
			
			List<? extends VariableElement> templateParams = templateMethod.getParameters();
			if (1 != templateParams.size() || !typeUtils.isSameType(templateParams.get(0).asType(), elementUtils.getTypeElement("com.fasterxml.jackson.databind.JsonNode").asType())) {
				messager.printMessage(
						Diagnostic.Kind.ERROR, 
						"@Template methods must take exactly one JsonNode parameter", 
						templateElement
				);
				errored = true;
				continue;
			}
			
		}
		
		return true;
	}
	
	private boolean isValidJavaIdentifier(String name) {
		if (null == name || "".equals(name)) {
			return false;
		}
		if (!Character.isJavaIdentifierStart(name.charAt(0))) {
			return false;
		}
		for (int i = 1; i < name.length(); i++) {
			if (!Character.isJavaIdentifierPart(name.charAt(i))) {
				return false;
			}
		}
		return true;
	}

}
