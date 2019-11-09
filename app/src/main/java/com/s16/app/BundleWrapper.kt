package com.s16.app

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.util.SparseArray
import java.io.Serializable

open class BundleWrapper(val bundle: Bundle?) : Parcelable {

    val data: Bundle
        get() = bundle ?: Bundle()

    constructor(parcel: Parcel) : this(parcel.readBundle()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeBundle(bundle)
    }

    override fun describeContents(): Int {
        return 0
    }

    /**
     * Inserts all mappings from the given Bundle into this Bundle.
     *
     * @param bundle a Bundle
     */
    fun putAll(bundle: Bundle) {
        data.putAll(bundle)
    }

    /**
     * Inserts a byte value into the mapping of this Bundle, replacing
     * any existing value for the given key.
     *
     * @param key a String, or null
     * @param value a byte
     */
    fun putByte(key: String?, value: Byte) {
        data.putByte(key, value)
    }

    fun putChar(key: String?, value: Char) {
        data.putChar(key, value)
    }

    /**
     * Inserts a short value into the mapping of this Bundle, replacing
     * any existing value for the given key.
     *
     * @param key a String, or null
     * @param value a short
     */
    fun putShort(key: String?, value: Short) {
        data.putShort(key, value)
    }

    /**
     * Inserts a float value into the mapping of this Bundle, replacing
     * any existing value for the given key.
     *
     * @param key a String, or null
     * @param value a float
     */
    fun putFloat(key: String?, value: Float) {
        data.putFloat(key, value)
    }

    /**
     * Inserts a CharSequence value into the mapping of this Bundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key a String, or null
     * @param value a CharSequence, or null
     */
    fun putCharSequence(key: String?, value: CharSequence?) {
        data.putCharSequence(key, value)
    }

    /**
     * Inserts a Parcelable value into the mapping of this Bundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key a String, or null
     * @param value a Parcelable object, or null
     */
    fun putParcelable(key: String?, value: Parcelable?) {
        data.putParcelable(key, value)
    }

    /**
     * Inserts an array of Parcelable values into the mapping of this Bundle,
     * replacing any existing value for the given key.  Either key or value may
     * be null.
     *
     * @param key a String, or null
     * @param value an array of Parcelable objects, or null
     */
    fun putParcelableArray(key: String?, value: Array<Parcelable>?) {
        data.putParcelableArray(key, value)
    }

    /**
     * Inserts a List of Parcelable values into the mapping of this Bundle,
     * replacing any existing value for the given key.  Either key or value may
     * be null.
     *
     * @param key a String, or null
     * @param value an ArrayList of Parcelable objects, or null
     */
    fun putParcelableArrayList(key: String?, value: java.util.ArrayList<out Parcelable>? ) {
        data.putParcelableArrayList(key, value)
    }

    /**
     * Inserts a SparceArray of Parcelable values into the mapping of this
     * Bundle, replacing any existing value for the given key.  Either key
     * or value may be null.
     *
     * @param key a String, or null
     * @param value a SparseArray of Parcelable objects, or null
     */
    fun putSparseParcelableArray(key: String?, value: SparseArray<out Parcelable>?) {
        data.putSparseParcelableArray(key, value)
    }

    /**
     * Inserts an ArrayList<Integer> value into the mapping of this Bundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key a String, or null
     * @param value an ArrayList<Integer> object, or null
    </Integer></Integer> */
    fun putIntegerArrayList(key: String?, value: ArrayList<Int>?) {
        data.putIntegerArrayList(key, value)
    }

    /**
     * Inserts an ArrayList<String> value into the mapping of this Bundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key a String, or null
     * @param value an ArrayList<String> object, or null
    </String></String> */
    fun putStringArrayList(key: String?, value: ArrayList<String>?) {
        data.putStringArrayList(key, value)
    }

    /**
     * Inserts an ArrayList<CharSequence> value into the mapping of this Bundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key a String, or null
     * @param value an ArrayList<CharSequence> object, or null
    </CharSequence></CharSequence> */
    fun putCharSequenceArrayList(key: String?, value: ArrayList<CharSequence>?) {
        data.putCharSequenceArrayList(key, value)
    }

    /**
     * Inserts a Serializable value into the mapping of this Bundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key a String, or null
     * @param value a Serializable object, or null
     */
    fun putSerializable(key: String?, value: Serializable?) {
        data.putSerializable(key, value)
    }

    /**
     * Inserts a byte array value into the mapping of this Bundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key a String, or null
     * @param value a byte array object, or null
     */
    fun putByteArray(key: String?, value: ByteArray?) {
        data.putByteArray(key, value)
    }

    /**
     * Inserts a short array value into the mapping of this Bundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key a String, or null
     * @param value a short array object, or null
     */
    fun putShortArray(key: String?, value: ShortArray?) {
        data.putShortArray(key, value)
    }

    /**
     * Inserts a char array value into the mapping of this Bundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key a String, or null
     * @param value a char array object, or null
     */
    fun putCharArray(key: String?, value: CharArray?) {
        data.putCharArray(key, value)
    }

    /**
     * Inserts a float array value into the mapping of this Bundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key a String, or null
     * @param value a float array object, or null
     */
    fun putFloatArray(key: String?, value: FloatArray?) {
        data.putFloatArray(key, value)
    }

    /**
     * Inserts a CharSequence array value into the mapping of this Bundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key a String, or null
     * @param value a CharSequence array object, or null
     */
    fun putCharSequenceArray(key: String?, value: Array<CharSequence>?) {
        data.putCharSequenceArray(key, value)
    }

    /**
     * Inserts a Bundle value into the mapping of this Bundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key a String, or null
     * @param value a Bundle object, or null
     */
    fun putBundle(key: String?, value: Bundle?) {
        data.putBundle(key, value)
    }

    /**
     * Returns the value associated with the given key, or (byte) 0 if
     * no mapping of the desired type exists for the given key.
     *
     * @param key a String
     * @return a byte value
     */
    fun getByte(key: String): Byte {
        return data.getByte(key)
    }

    /**
     * Returns the value associated with the given key, or defaultValue if
     * no mapping of the desired type exists for the given key.
     *
     * @param key a String
     * @param defaultValue Value to return if key does not exist
     * @return a byte value
     */
    fun getByte(key: String, defaultValue: Byte): Byte? {
        return data.getByte(key, defaultValue)
    }

    /**
     * Returns the value associated with the given key, or (char) 0 if
     * no mapping of the desired type exists for the given key.
     *
     * @param key a String
     * @return a char value
     */
    fun getChar(key: String): Char {
        return data.getChar(key)
    }

    /**
     * Returns the value associated with the given key, or defaultValue if
     * no mapping of the desired type exists for the given key.
     *
     * @param key a String
     * @param defaultValue Value to return if key does not exist
     * @return a char value
     */
    fun getChar(key: String, defaultValue: Char): Char {
        return data.getChar(key, defaultValue)
    }

    /**
     * Returns the value associated with the given key, or (short) 0 if
     * no mapping of the desired type exists for the given key.
     *
     * @param key a String
     * @return a short value
     */
    fun getShort(key: String): Short {
        return data.getShort(key)
    }

    /**
     * Returns the value associated with the given key, or defaultValue if
     * no mapping of the desired type exists for the given key.
     *
     * @param key a String
     * @param defaultValue Value to return if key does not exist
     * @return a short value
     */
    fun getShort(key: String, defaultValue: Short): Short {
        return data.getShort(key, defaultValue)
    }

    /**
     * Returns the value associated with the given key, or 0.0f if
     * no mapping of the desired type exists for the given key.
     *
     * @param key a String
     * @return a float value
     */
    fun getFloat(key: String): Float {
        return data.getFloat(key)
    }

    /**
     * Returns the value associated with the given key, or defaultValue if
     * no mapping of the desired type exists for the given key.
     *
     * @param key a String
     * @param defaultValue Value to return if key does not exist
     * @return a float value
     */
    fun getFloat(key: String, defaultValue: Float): Float {
        return data.getFloat(key, defaultValue)
    }

    /**
     * Returns the value associated with the given key, or null if
     * no mapping of the desired type exists for the given key or a null
     * value is explicitly associated with the key.
     *
     * @param key a String, or null
     * @return a CharSequence value, or null
     */
    fun getCharSequence(key: String?): CharSequence? {
        return data.getCharSequence(key)
    }

    /**
     * Returns the value associated with the given key, or defaultValue if
     * no mapping of the desired type exists for the given key or if a null
     * value is explicitly associatd with the given key.
     *
     * @param key a String, or null
     * @param defaultValue Value to return if key does not exist or if a null
     * value is associated with the given key.
     * @return the CharSequence value associated with the given key, or defaultValue
     * if no valid CharSequence object is currently mapped to that key.
     */
    fun getCharSequence(key: String?, defaultValue: CharSequence): CharSequence {
        return data.getCharSequence(key, defaultValue)
    }

    /**
     * Returns the value associated with the given key, or null if
     * no mapping of the desired type exists for the given key or a null
     * value is explicitly associated with the key.
     *
     * @param key a String, or null
     * @return a Bundle value, or null
     */
    fun getBundle(key: String?): Bundle? {
        return data.getBundle(key)
    }

    /**
     * Returns the value associated with the given key, or null if
     * no mapping of the desired type exists for the given key or a null
     * value is explicitly associated with the key.
     *
     * @param key a String, or null
     * @return a Parcelable value, or null
     */
    fun <T : Parcelable> getParcelable(key: String?): T? {
        return data.getParcelable(key)
    }

    /**
     * Returns the value associated with the given key, or null if
     * no mapping of the desired type exists for the given key or a null
     * value is explicitly associated with the key.
     *
     * @param key a String, or null
     * @return a Parcelable[] value, or null
     */
    fun getParcelableArray(key: String?): Array<Parcelable>? {
        return data.getParcelableArray(key)
    }

    /**
     * Returns the value associated with the given key, or null if
     * no mapping of the desired type exists for the given key or a null
     * value is explicitly associated with the key.
     *
     * @param key a String, or null
     * @return an ArrayList<T> value, or null
    </T> */
    fun <T : Parcelable> getParcelableArrayList(key: String?): ArrayList<T>? {
        return data.getParcelableArrayList(key)
    }

    /**
     * Returns the value associated with the given key, or null if
     * no mapping of the desired type exists for the given key or a null
     * value is explicitly associated with the key.
     *
     * @param key a String, or null
     *
     * @return a SparseArray of T values, or null
     */
    fun <T : Parcelable> getSparseParcelableArray(key: String?): SparseArray<T>? {
        return data.getSparseParcelableArray(key)
    }

    /**
     * Returns the value associated with the given key, or null if
     * no mapping of the desired type exists for the given key or a null
     * value is explicitly associated with the key.
     *
     * @param key a String, or null
     * @return a Serializable value, or null
     */
    fun getSerializable(key: String?): Serializable? {
        return data.getSerializable(key)
    }

    /**
     * Returns the value associated with the given key, or null if
     * no mapping of the desired type exists for the given key or a null
     * value is explicitly associated with the key.
     *
     * @param key a String, or null
     * @return an ArrayList<String> value, or null
    </String> */
    fun getIntegerArrayList(key: String?): ArrayList<Int>? {
        return data.getIntegerArrayList(key)
    }

    /**
     * Returns the value associated with the given key, or null if
     * no mapping of the desired type exists for the given key or a null
     * value is explicitly associated with the key.
     *
     * @param key a String, or null
     * @return an ArrayList<String> value, or null
    </String> */
    fun getStringArrayList(key: String?): ArrayList<String>? {
        return data.getStringArrayList(key)
    }

    /**
     * Returns the value associated with the given key, or null if
     * no mapping of the desired type exists for the given key or a null
     * value is explicitly associated with the key.
     *
     * @param key a String, or null
     * @return an ArrayList<CharSequence> value, or null
    </CharSequence> */
    fun getCharSequenceArrayList(key: String?): ArrayList<CharSequence>? {
        return data.getCharSequenceArrayList(key)
    }

    /**
     * Returns the value associated with the given key, or null if
     * no mapping of the desired type exists for the given key or a null
     * value is explicitly associated with the key.
     *
     * @param key a String, or null
     * @return a byte[] value, or null
     */
    fun getByteArray(key: String?): ByteArray? {
        return data.getByteArray(key)
    }

    /**
     * Returns the value associated with the given key, or null if
     * no mapping of the desired type exists for the given key or a null
     * value is explicitly associated with the key.
     *
     * @param key a String, or null
     * @return a short[] value, or null
     */
    fun getShortArray(key: String?): ShortArray? {
        return data.getShortArray(key)
    }

    /**
     * Returns the value associated with the given key, or null if
     * no mapping of the desired type exists for the given key or a null
     * value is explicitly associated with the key.
     *
     * @param key a String, or null
     * @return a char[] value, or null
     */
    fun getCharArray(key: String?): CharArray? {
        return data.getCharArray(key)
    }

    /**
     * Returns the value associated with the given key, or null if
     * no mapping of the desired type exists for the given key or a null
     * value is explicitly associated with the key.
     *
     * @param key a String, or null
     * @return a float[] value, or null
     */
    fun getFloatArray(key: String?): FloatArray? {
        return data.getFloatArray(key)
    }

    /**
     * Returns the value associated with the given key, or null if
     * no mapping of the desired type exists for the given key or a null
     * value is explicitly associated with the key.
     *
     * @param key a String, or null
     * @return a CharSequence[] value, or null
     */
    fun getCharSequenceArray(key: String?): Array<CharSequence>? {
        return data.getCharSequenceArray(key)
    }

    override fun toString(): String {
        return data.toString()
    }

    companion object CREATOR : Parcelable.Creator<BundleWrapper> {
        override fun createFromParcel(parcel: Parcel): BundleWrapper {
            return BundleWrapper(parcel)
        }

        override fun newArray(size: Int): Array<BundleWrapper?> {
            return arrayOfNulls(size)
        }
    }

}