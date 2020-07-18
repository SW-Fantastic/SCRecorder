#include<Windows.h>
#include <mmsystem.h>

#include"org_swdc_recorder_core_platform_Devices.h"
#pragma comment(lib, "Winmm.lib")


jstring w2js(JNIEnv* env, wchar_t* src);

JNIEXPORT jobject JNICALL Java_org_swdc_recorder_core_platform_Devices_getAudioDevices
(JNIEnv* env, jclass clazz) {
	jclass listClazz = env->FindClass("java/util/ArrayList");
	jmethodID listConstructor = env->GetMethodID(listClazz, "<init>", "()V");
	jmethodID addItem = env->GetMethodID(listClazz, "add", "(Ljava/lang/Object;)Z");
	
	jclass procItemClazz = env->FindClass("org/swdc/recorder/core/platform/MediaDevice");
	jmethodID mediaConstructor = env->GetMethodID(procItemClazz, "<init>", "(Ljava/lang/String;IZZ)V");
	int soundDeviceCounts = waveOutGetNumDevs();

	jobject arrayList = env->NewObject(listClazz, listConstructor);

	if (soundDeviceCounts == 0) {
		return arrayList;
	}

	for (int i = 0; i < soundDeviceCounts; i++) {
		WAVEINCAPS wic;
		int rs = waveInGetDevCaps(i, &wic, sizeof(wic));
		if (rs == 0){
			jobject deviceIn = env->NewObject(procItemClazz, mediaConstructor, w2js(env, wic.szPname), wic.wMid, true, false);
			env->CallVoidMethod(arrayList, addItem, deviceIn);
		}
		
		WAVEOUTCAPS woc;
		rs = waveOutGetDevCaps(i, &woc, sizeof(woc));
		if (rs == 0) {
			jobject deviceOut = env->NewObject(procItemClazz, mediaConstructor, w2js(env, woc.szPname), woc.wMid, false, true);
			env->CallVoidMethod(arrayList, addItem, deviceOut);
		}
	}
	return arrayList;
}

jstring w2js(JNIEnv* env, wchar_t* src) {
	int src_len = wcslen(src);
	jchar* dest = new jchar[src_len + 1];
	memset(dest, 0, sizeof(jchar) * (src_len + 1));

	for (int i = 0; i < src_len; i++) {
		memcpy(&dest[i], &src[i], 2);
	}
	jstring dst = env->NewString(dest, src_len);
	delete[] dest;
	return dst;
}