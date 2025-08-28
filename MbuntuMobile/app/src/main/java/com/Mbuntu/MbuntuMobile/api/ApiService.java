package com.Mbuntu.MbuntuMobile.api;

import com.Mbuntu.MbuntuMobile.api.models.AuthenticationRequest;
import com.Mbuntu.MbuntuMobile.api.models.AuthenticationResponse;
import com.Mbuntu.MbuntuMobile.api.models.ConversationResponse;
import com.Mbuntu.MbuntuMobile.api.models.CreateConversationRequest;
import com.Mbuntu.MbuntuMobile.api.models.MessageResponse;
import com.Mbuntu.MbuntuMobile.api.models.RegisterRequest;
import com.Mbuntu.MbuntuMobile.api.models.SendMessageRequest;
import com.Mbuntu.MbuntuMobile.api.models.UpdateProfileRequest;
import com.Mbuntu.MbuntuMobile.api.models.UserProfileResponse;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.DELETE;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    // --- AUTH ---
    @POST("api/auth/register")
    Call<AuthenticationResponse> register(@Body RegisterRequest request);

    @POST("api/auth/login")
    Call<AuthenticationResponse> login(@Body AuthenticationRequest request);

    // --- CONVERSATIONS ---
    @GET("api/v1/conversations")
    Call<List<ConversationResponse>> getConversations(@Header("Authorization") String token);

    @GET("api/v1/users/search")
    Call<List<UserProfileResponse>> searchUsers(
            @Header("Authorization") String token,
            @Query("query") String query);

    @POST("api/v1/conversations")
    Call<ConversationResponse> createConversation(@Header("Authorization") String token, @Body CreateConversationRequest request);

    // --- MESSAGES ---
    @GET("api/v1/messages/conversation/{conversationId}")
    Call<List<MessageResponse>> getMessagesForConversation(@Header("Authorization") String token, @Path("conversationId") long conversationId);

    @POST("api/v1/messages")
    Call<MessageResponse> sendMessage(@Header("Authorization") String token, @Body SendMessageRequest request);

    @DELETE("api/v1/messages/{messageId}")
    Call<Void> deleteMessage(
            @Header("Authorization") String token,
            @Path("messageId") long messageId
    );

    // --- USERS / PROFILE ---
    @GET("api/v1/users/me")
    Call<UserProfileResponse> getMyProfile(@Header("Authorization") String token);

    @PUT("api/v1/users/me")
    Call<UserProfileResponse> updateMyProfile(@Header("Authorization") String token, @Body UpdateProfileRequest request);

    // Pour l'upload, la réponse est un JSON simple, donc on peut le mapper dans une Map générique.
    @Multipart
    @POST("api/v1/users/me/picture")
    Call<Map<String, String>> uploadProfilePicture(@Header("Authorization") String token, @Part MultipartBody.Part file);
}