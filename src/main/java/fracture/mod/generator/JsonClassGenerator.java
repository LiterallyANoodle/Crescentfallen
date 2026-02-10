package fracture.mod.generator;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import com.google.auto.service.AutoService;

@AutoService(Processor.class)
public class JsonClassGenerator extends AbstractProcessor {
	
	private Types typeUtils;
	private Elements elementUtils;
	private Filer filer;
	private Messager messager; 
	private Map<String, String> factoryClasses = new LinkedHashMap<String, String>();
	
	@Override
	public synchronized void init(ProcessingEnvironment env) {
		
	}
	
	@Override 
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
		return false;
	}
	
	@Override
	public Set<String> getSupportedAnnotationTypes() {
		return null;
	}
	
	@Override
	public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.latestSupported();
	}

}
