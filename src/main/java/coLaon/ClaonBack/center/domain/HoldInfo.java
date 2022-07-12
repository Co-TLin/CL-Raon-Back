package coLaon.ClaonBack.center.domain;

import coLaon.ClaonBack.common.domain.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Getter
@Setter
@Table(name = "tb_hold_info")
@NoArgsConstructor
public class HoldInfo extends BaseEntity {
    @Column(name = "name", nullable = false)
    private String name;

    @ManyToOne(targetEntity = Center.class)
    @JoinColumn(name = "center_id", nullable = false)
    private Center center;

    private HoldInfo(
            String name,
            Center center
    ) {
        this.name = name;
        this.center = center;
    }

    public static HoldInfo of(
            String name,
            Center center
    ) {
        return new HoldInfo(name, center);
    }
}
