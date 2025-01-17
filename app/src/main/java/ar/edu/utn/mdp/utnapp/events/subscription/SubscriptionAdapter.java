package ar.edu.utn.mdp.utnapp.events.subscription;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import ar.edu.utn.mdp.utnapp.R;
import ar.edu.utn.mdp.utnapp.SubjectActivity;
import ar.edu.utn.mdp.utnapp.errors.ErrorDialog;
import ar.edu.utn.mdp.utnapp.fetch.callbacks.CallBackRequest;
import ar.edu.utn.mdp.utnapp.fetch.models.Subject;
import ar.edu.utn.mdp.utnapp.fetch.request.commission.CommissionModel;

public class SubscriptionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_FOOTER = 1;
    private static final int TYPE_ITEM = 2;
    ArrayList<String> subscriptions;

    public SubscriptionAdapter(HashSet<String> subscriptions) {
        ArrayList<String> list = new ArrayList<>(subscriptions);

        list.sort(byYearComSubject());

        this.subscriptions = list;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_ITEM) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_subscription, parent, false);
            return new SubscriptionAdapter.ViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.footer_subscription_fragment, parent, false);
            return new SubscriptionAdapter.FooterViewHolder(view);
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof SubscriptionAdapter.ViewHolder && position < subscriptions.size()) {
            Subject subject = Subject.split(subscriptions.get(position));

            String ordinal = ordinals(subject.getYear());
            ordinal = ordinal.substring(ordinal.length() - 2);

            ((ViewHolder) holder).subject.setText(subject.getSubject());
            ((ViewHolder) holder).com_year.setText("Comisión " + subject.getCommission() + "  ·  " + subject.getYear() + ordinal + " año");
        }

    }

    @Override
    public int getItemCount() {
        return subscriptions.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == subscriptions.size()) {
            return TYPE_FOOTER;
        }
        return TYPE_ITEM;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView subject;
        private final TextView com_year;

        public ViewHolder(@NonNull View view) {
            super(view);

            subject = view.findViewById(R.id.subscription_subject);
            com_year = view.findViewById(R.id.subscription_com_year);
            view.setOnClickListener(v -> {
                String comYear = (String) com_year.getText();
                String subjectStr = (String) subject.getText();
                Map<String, String> comYearMap = splitComYearText(comYear);

                Subject subject = new Subject(subjectStr,
                        Integer.parseInt(comYearMap.get("year")),
                        Integer.parseInt(comYearMap.get("commission")));

                Dialog dialog = new Dialog(v.getContext());
                CommissionModel.getSubjectByCommission(view.getContext(), subject, new CallBackRequest<JSONObject>() {
                    @Override
                    public void onSuccess(JSONObject response) {
                        dialog.dismiss();
                        Intent intent = new Intent(v.getContext(), SubjectActivity.class);
                        intent.putExtra("subject", response.toString());
                        v.getContext().startActivity(intent);
                    }

                    @Override
                    public void onError(int statusCode) {
                        dialog.dismiss();
                        new ErrorDialog(
                                view.getContext(),
                                "Lo sentimos... 😥",
                                subjectStr + " aún no se encuentra cargada en la base de datos.",
                                R.drawable.ic_warning);

                    }
                });
            });
        }
    }

    public static class FooterViewHolder extends RecyclerView.ViewHolder {

        private final ImageView icon;

        public FooterViewHolder(@NonNull View view) {
            super(view);

            icon = view.findViewById(R.id.footer_subscription_icon);
        }
    }

    private static Map<String, String> splitComYearText(String comYear) {
        String[] str = comYear.split("·");
        String commission = str[0].split("Comisión ")[1].trim();
        String year = str[1].split(" año")[0].trim();
        year = year.substring(0, year.length() - 2);

        Map<String, String> map = new HashMap<>();
        map.put("commission", commission);
        map.put("year", year);

        return map;
    }

    @NonNull
    private Comparator<String> byYearComSubject() {
        return (o1, o2) -> {
            Subject s1 = Subject.split(o1);
            Subject s2 = Subject.split(o2);

            if (s1.getYear() < s2.getYear()) {
                return -1;
            } else if (s1.getYear() > s2.getYear()) {
                return 1;
            } else {
                if (s1.getCommission() < s2.getCommission()) {
                    return -1;
                } else if (s1.getCommission()  > s2.getCommission()) {
                    return 1;
                } else {
                    return Integer.compare(s1.getSubject().compareTo(s1.getSubject()), 0);
                }
            }
        };
    }

    private String ordinals(int num) {
        final String[] ordinals = {"Primero", "Segundo", "Tercero", "Cuarto"};
        return ordinals[num - 1];
    }
}
