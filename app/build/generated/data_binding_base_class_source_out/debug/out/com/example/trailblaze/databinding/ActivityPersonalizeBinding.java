// Generated by view binder compiler. Do not edit!
package com.example.trailblaze.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.example.trailblaze.R;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class ActivityPersonalizeBinding implements ViewBinding {
  @NonNull
  private final LinearLayout rootView;

  @NonNull
  public final TextView Personalizehost;

  @NonNull
  public final ImageButton chevronLeft;

  @NonNull
  public final FrameLayout fragmentContainer;

  private ActivityPersonalizeBinding(@NonNull LinearLayout rootView,
      @NonNull TextView Personalizehost, @NonNull ImageButton chevronLeft,
      @NonNull FrameLayout fragmentContainer) {
    this.rootView = rootView;
    this.Personalizehost = Personalizehost;
    this.chevronLeft = chevronLeft;
    this.fragmentContainer = fragmentContainer;
  }

  @Override
  @NonNull
  public LinearLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static ActivityPersonalizeBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static ActivityPersonalizeBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.activity_personalize, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static ActivityPersonalizeBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.Personalizehost;
      TextView Personalizehost = ViewBindings.findChildViewById(rootView, id);
      if (Personalizehost == null) {
        break missingId;
      }

      id = R.id.chevron_left;
      ImageButton chevronLeft = ViewBindings.findChildViewById(rootView, id);
      if (chevronLeft == null) {
        break missingId;
      }

      id = R.id.fragmentContainer;
      FrameLayout fragmentContainer = ViewBindings.findChildViewById(rootView, id);
      if (fragmentContainer == null) {
        break missingId;
      }

      return new ActivityPersonalizeBinding((LinearLayout) rootView, Personalizehost, chevronLeft,
          fragmentContainer);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}