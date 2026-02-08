package fracture.mod.generator;

import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;

import com.google.auto.service.AutoService;

@AutoService(Processor.class)
public class ItemRegistrationGenerator extends AbstractProcessor {
	
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
