//Concept taken from http://javapapers.com/core-java/how-to-call-a-c-program-from-java/

#include stdio.h
#include jni.h
#include "GameLogic.h"
JNIEXPORT void JNICALL Java_JavaToC_helloC(JNIEnv *env, jobject javaobj)
{
	printf("Hello World: From C");
	return;
}