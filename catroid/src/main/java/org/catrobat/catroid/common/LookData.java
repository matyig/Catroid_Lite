/*
 * Catroid: An on-device visual programming system for Android devices
 * Copyright (C) 2010-2016 The Catrobat Team
 * (<http://developer.catrobat.org/credits>)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * An additional term exception under section 7 of the GNU Affero
 * General Public License, version 3, is available at
 * http://developer.catrobat.org/license_additional_term
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.catrobat.catroid.common;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import org.catrobat.catroid.ProjectManager;
import org.catrobat.catroid.content.Scene;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.bricks.Brick;
import org.catrobat.catroid.utils.ImageEditing;
import org.catrobat.catroid.utils.Utils;

import java.io.FileNotFoundException;
import java.io.Serializable;

public class LookData implements Serializable, Cloneable {
	private static final long serialVersionUID = 1L;
	private static final String TAG = LookData.class.getSimpleName();

	@XStreamAsAttribute
	protected String name;
	protected String fileName;
	protected transient Bitmap thumbnailBitmap;
	protected transient Integer width;
	protected transient Integer height;
	protected static final transient int THUMBNAIL_WIDTH = 150;
	protected static final transient int THUMBNAIL_HEIGHT = 150;
	protected transient Pixmap pixmap = null;
	protected transient Pixmap originalPixmap = null;
	protected transient TextureRegion textureRegion = null;
	public transient boolean isBackpackLookData = false;

	public LookData() {
	}

	public void draw(Batch batch, float alpha) {
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof LookData)) {
			return false;
		}
		if (obj == this) {
			return true;
		}

		LookData lookData = (LookData) obj;
		if (lookData.fileName.equals(this.fileName) && lookData.name.equals(this.name)) {
			return true;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return name.hashCode() + fileName.hashCode() + super.hashCode();
	}

	@Override
	public LookData clone() {
		LookData cloneLookData = new LookData();

		cloneLookData.name = this.name;
		cloneLookData.fileName = this.fileName;
		String filePath = getPathToImageDirectory() + "/" + fileName;
		cloneLookData.isBackpackLookData = false;
		try {
			ProjectManager.getInstance().getFileChecksumContainer().incrementUsage(filePath);
		} catch (FileNotFoundException fileNotFoundexception) {
			Log.e(TAG, Log.getStackTraceString(fileNotFoundexception));
		}

		return cloneLookData;
	}

	public void resetLookData() {
		pixmap = null;
		originalPixmap = null;
		textureRegion = null;
	}

	public TextureRegion getTextureRegion() {
		if (textureRegion == null) {
			textureRegion = new TextureRegion(new Texture(getPixmap()));
		}
		return textureRegion;
	}

	public void setTextureRegion() {
		this.textureRegion = new TextureRegion(new Texture(getPixmap()));
	}

	public Pixmap getPixmap() {
		if (pixmap == null) {
			try {
				pixmap = new Pixmap(Gdx.files.absolute(getAbsolutePath()));
			} catch (GdxRuntimeException gdxRuntimeException) {
				Log.e(TAG, "gdx.files throws GdxRuntimeException", gdxRuntimeException);
				if (gdxRuntimeException.getMessage().startsWith("Couldn't load file:")) {
					pixmap = new Pixmap(1, 1, Pixmap.Format.Alpha);
				}
			} catch (NullPointerException nullPointerException) {
				Log.e(TAG, "gdx.files throws NullPointerException", nullPointerException);
			}
		}
		return pixmap;
	}

	public void setPixmap(Pixmap pixmap) {
		this.pixmap = pixmap;
	}

	public Pixmap getOriginalPixmap() {
		if (originalPixmap == null) {
			originalPixmap = new Pixmap(Gdx.files.absolute(getAbsolutePath()));
		}
		return originalPixmap;
	}

	public String getAbsolutePath() {
		if (fileName != null) {
			if (isBackpackLookData) {
				return Utils.buildPath(getPathToBackPackImageDirectory(), fileName);
			} else {
				return Utils.buildPath(getPathToImageDirectory(), fileName);
			}
		} else {
			return null;
		}
	}

	public String getAbsoluteBackPackPath() {
		if (fileName != null) {
			return Utils.buildPath(getPathToBackPackImageDirectory(), fileName);
		} else {
			return null;
		}
	}

	public String getAbsoluteProjectPath() {
		if (fileName != null) {
			return Utils.buildPath(getPathToImageDirectory(), fileName);
		} else {
			return null;
		}
	}

	public String getLookName() {
		return name;
	}

	public void setLookName(String name) {
		this.name = name;
	}

	public void setLookFilename(String fileName) {
		this.fileName = fileName;
	}

	public String getLookFileName() {
		return fileName;
	}

	public String getChecksum() {
		if (fileName == null) {
			return null;
		}
		return fileName.substring(0, 32);
	}

	protected String getPathToImageDirectory() {
		return Utils.buildPath(Utils.buildProjectPath(ProjectManager.getInstance().getCurrentProject().getName()),
				getSceneNameByLookData(), Constants.IMAGE_DIRECTORY);
	}

	protected String getSceneNameByLookData() {
		for (Scene scene : ProjectManager.getInstance().getCurrentProject().getSceneList()) {
			for (Sprite sprite : scene.getSpriteList()) {
				if (sprite.getLookDataList().contains(this)) {
					return scene.getName();
				}
			}
		}
		return ProjectManager.getInstance().getCurrentScene().getName();
	}

	private String getPathToBackPackImageDirectory() {
		return Utils.buildPath(Constants.DEFAULT_ROOT, Constants.BACKPACK_DIRECTORY,
				Constants.BACKPACK_IMAGE_DIRECTORY);
	}

	public Bitmap getThumbnailBitmap() {
		if (thumbnailBitmap == null) {
			thumbnailBitmap = ImageEditing.getScaledBitmapFromPath(getAbsolutePath(), THUMBNAIL_HEIGHT,
					THUMBNAIL_WIDTH, ImageEditing.ResizeType.STAY_IN_RECTANGLE_WITH_SAME_ASPECT_RATIO, false);
		}
		return thumbnailBitmap;
	}

	public void resetThumbnailBitmap() {
		thumbnailBitmap = null;
	}

	public int[] getMeasure() {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(getAbsolutePath(), options);
		width = options.outWidth;
		height = options.outHeight;

		return new int[] { width, height };
	}

	@Override
	public String toString() {
		return name;
	}

	public int getRequiredResources() {
		return Brick.NO_RESOURCES;
	}
}
