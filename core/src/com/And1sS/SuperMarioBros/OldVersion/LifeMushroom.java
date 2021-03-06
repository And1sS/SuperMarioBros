package com.And1sS.SuperMarioBros.OldVersion;

public class LifeMushroom extends Enemy {

	public LifeMushroom(float x, float y, float width, float height) {
		super(x, y, width, height, new Anim("images/objects.png", 1, 0, 0, 16, 16, -1, -1, -1, -1, 0, true));
	}

	@Override
	public void update(Level level, Mario mario) {
		// TODO: Implement this method
		super.update(level, mario);
		if(mario.getBody().getBounds().overlaps(getBody().getBounds())) {
			delete();
			mario.superMario(level);
		}
	}
}
