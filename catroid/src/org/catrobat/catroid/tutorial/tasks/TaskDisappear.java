package org.catrobat.catroid.tutorial.tasks;

import java.util.HashMap;

import org.catrobat.catroid.tutorial.SurfaceObjectTutor;

public class TaskDisappear implements Task {
	private Tutor tutorType;

	@Override
	public Tutor getTutorType() {
		return tutorType;
	}

	public void setTutorType(Tutor tutorType) {
		this.tutorType = tutorType;
	}

	@Override
	public Type getType() {
		return (Type.DISAPPEAR);
	}

	@Override
	public boolean execute(HashMap<Task.Tutor, SurfaceObjectTutor> tutors) {
		SurfaceObjectTutor tutor = tutors.get(tutorType);
		if (tutor != null) {
			tutor.disappear();
		}
		return true;
	}

	@Override
	public void setEndPositionOfTaskForTutor(HashMap<Tutor, SurfaceObjectTutor> tutors) {
	}
}
