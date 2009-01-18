package de.knewcleus.openradar.adexp;

public interface IBasicField extends IField {
	public Object getValue();
	
	@Override
	public IBasicFieldDescriptor getDescriptor();
	
	public String valueToString();
}
