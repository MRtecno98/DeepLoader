package me.tecno.loader.descriptor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ClearablePool {
	private List<Clearable> pool = new ArrayList<>();
	
	public ClearablePool add(Clearable... clearables) {
		getPool().addAll(Arrays.asList(clearables));
		return this;
	}
	
	public void clearAll() {
		getPool().forEach(Clearable::clear);
	}
}
