package com.yourgroup.studypulseai.network

import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

object SupabaseAuthHelper {
    private val scope = CoroutineScope(Dispatchers.IO)

    interface AuthCallback {
        fun onResult(success: Boolean, error: String?)
    }

    interface SignOutCallback {
        fun onResult(success: Boolean)
    }

    @JvmStatic
    fun isLoggedIn(): Boolean {
        return try {
            SupabaseManager.client.auth.currentSessionOrNull() != null
        } catch (e: Exception) {
            false
        }
    }

    @JvmStatic
    fun getCurrentUserId(): String? {
        return SupabaseManager.client.auth.currentSessionOrNull()?.user?.id
    }

    @JvmStatic
    fun getCurrentUserEmail(): String? {
        return SupabaseManager.client.auth.currentSessionOrNull()?.user?.email
    }

    @JvmStatic
    fun getCurrentUserName(): String? {
        val user = SupabaseManager.client.auth.currentSessionOrNull()?.user
        return user?.userMetadata?.get("display_name")?.toString()?.replace("\"", "")
    }

    @JvmStatic
    fun updateProfileName(newName: String, callback: AuthCallback) {
        scope.launch {
            try {
                SupabaseManager.client.auth.updateUser {
                    data = buildJsonObject {
                        put("display_name", newName)
                    }
                }
                withContext(Dispatchers.Main) {
                    callback.onResult(true, null)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    callback.onResult(false, e.message)
                }
            }
        }
    }

    @JvmStatic
    fun changePassword(newPass: String, callback: AuthCallback) {
        scope.launch {
            try {
                SupabaseManager.client.auth.updateUser {
                    password = newPass
                }
                withContext(Dispatchers.Main) {
                    callback.onResult(true, null)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    callback.onResult(false, e.message)
                }
            }
        }
    }

    @JvmStatic
    fun signIn(email: String, pass: String, callback: AuthCallback) {
        scope.launch {
            try {
                SupabaseManager.client.auth.signInWith(Email) {
                    this.email = email
                    password = pass
                }
                withContext(Dispatchers.Main) {
                    callback.onResult(true, null)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    callback.onResult(false, e.message)
                }
            }
        }
    }

    @JvmStatic
    fun signUp(email: String, pass: String, name: String, callback: AuthCallback) {
        scope.launch {
            try {
                SupabaseManager.client.auth.signUpWith(Email) {
                    this.email = email
                    password = pass
                    data = buildJsonObject {
                        put("display_name", name)
                    }
                }
                withContext(Dispatchers.Main) {
                    callback.onResult(true, null)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    callback.onResult(false, e.message)
                }
            }
        }
    }

    @JvmStatic
    fun signOut(callback: SignOutCallback) {
        scope.launch {
            try {
                SupabaseManager.client.auth.signOut()
                withContext(Dispatchers.Main) {
                    callback.onResult(true)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    callback.onResult(false)
                }
            }
        }
    }
}
