// Generated by view binder compiler. Do not edit!
package com.example.trailblaze.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.example.trailblaze.R;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class FragmentProfileBinding implements ViewBinding {
  @NonNull
  private final ScrollView rootView;

  @NonNull
  public final TextView accountTitle;

  @NonNull
  public final ImageButton editProfileButton;

  @NonNull
  public final ImageButton iconDifficulty;

  @NonNull
  public final ImageButton iconLocation;

  @NonNull
  public final ImageButton profileImage;

  @NonNull
  public final TextView username;

  @NonNull
  public final TextView watcherMember;

  private FragmentProfileBinding(@NonNull ScrollView rootView, @NonNull TextView accountTitle,
      @NonNull ImageButton editProfileButton, @NonNull ImageButton iconDifficulty,
      @NonNull ImageButton iconLocation, @NonNull ImageButton profileImage,
      @NonNull TextView username, @NonNull TextView watcherMember) {
    this.rootView = rootView;
    this.accountTitle = accountTitle;
    this.editProfileButton = editProfileButton;
    this.iconDifficulty = iconDifficulty;
    this.iconLocation = iconLocation;
    this.profileImage = profileImage;
    this.username = username;
    this.watcherMember = watcherMember;
  }

  @Override
  @NonNull
  public ScrollView getRoot() {
    return rootView;
  }

  @NonNull
  public static FragmentProfileBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static FragmentProfileBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.fragment_profile, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static FragmentProfileBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.account_title;
      TextView accountTitle = ViewBindings.findChildViewById(rootView, id);
      if (accountTitle == null) {
        break missingId;
      }

      id = R.id.edit_profile_button;
      ImageButton editProfileButton = ViewBindings.findChildViewById(rootView, id);
      if (editProfileButton == null) {
        break missingId;
      }

      id = R.id.icon_difficulty;
      ImageButton iconDifficulty = ViewBindings.findChildViewById(rootView, id);
      if (iconDifficulty == null) {
        break missingId;
      }

      id = R.id.icon_location;
      ImageButton iconLocation = ViewBindings.findChildViewById(rootView, id);
      if (iconLocation == null) {
        break missingId;
      }

      id = R.id.profile_image;
      ImageButton profileImage = ViewBindings.findChildViewById(rootView, id);
      if (profileImage == null) {
        break missingId;
      }

      id = R.id.username;
      TextView username = ViewBindings.findChildViewById(rootView, id);
      if (username == null) {
        break missingId;
      }

      id = R.id.watcher_member;
      TextView watcherMember = ViewBindings.findChildViewById(rootView, id);
      if (watcherMember == null) {
        break missingId;
      }

      return new FragmentProfileBinding((ScrollView) rootView, accountTitle, editProfileButton,
          iconDifficulty, iconLocation, profileImage, username, watcherMember);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}