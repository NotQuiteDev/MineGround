package org.battle.mineground.pointer;

import org.battle.mineground.MineGround;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PointerCommandExecutor implements CommandExecutor {

    private final MineGround plugin;

    public PointerCommandExecutor(MineGround plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            // 명령어 인자로 목표 좌표가 정확히 입력되었는지 확인 (x, y, z)
            if (args.length == 3) {
                try {
                    double x = Double.parseDouble(args[0]);
                    double y = Double.parseDouble(args[1]);
                    double z = Double.parseDouble(args[2]);

                    // 입력된 좌표로 목표 위치 설정
                    Location targetLocation = new Location(player.getWorld(), x, y, z);

                    // 플레이어 주변에서 목표 지점을 향하는 화살표 그리기
                    drawArrowTowardsTarget(player, targetLocation);
                    return true;

                } catch (NumberFormatException e) {
                    player.sendMessage("좌표는 숫자로 입력해야 합니다.");
                    return false;
                }
            } else {
                player.sendMessage("명령어 사용법: /showpointer <x> <y> <z>");
                return false;
            }
        }
        return false;
    }

    // 목표 지점을 향해 화살표 모양을 그리는 메소드
    public void drawArrowTowardsTarget(Player player, Location targetLocation) {
        Location playerLocation = player.getLocation();
        double directionX = targetLocation.getX() - playerLocation.getX();
        double directionZ = targetLocation.getZ() - playerLocation.getZ();

        // 목표 방향에 대한 각도 계산
        double angle = Math.atan2(directionZ, directionX);

        // 화살표 줄기 생성 (길이를 1.5로 제한)
        double arrowLength = 1.5;
        drawArrowShaft(player, playerLocation, angle, arrowLength);

        // 화살표 머리 생성 (줄기 끝에 배치)
        Location arrowHeadLocation = playerLocation.clone().add(Math.cos(angle) * arrowLength, 0, Math.sin(angle) * arrowLength);
        drawArrowHead(player, arrowHeadLocation, angle);
    }

    // 화살표의 줄기 부분을 그리는 메소드 (길이를 1.5로 제한)
    public void drawArrowShaft(Player player, Location start, double angle, double length) {
        int points = (int) (length * 5); // 줄기의 길이에 따른 점 개수
        for (int i = 0; i < points; i++) {
            double distance = length * i / points;
            double x = distance * Math.cos(angle);
            double z = distance * Math.sin(angle);

            Location particleLocation = start.clone().add(x, 0, z);
            player.spawnParticle(Particle.FLAME, particleLocation, 0, 0, 0, 0, 0);
        }
    }

    // 화살표의 머리 부분을 그리는 메소드 (줄기 끝에 생성)
    public void drawArrowHead(Player player, Location end, double angle) {
        double headSize = 0.5; // 화살표 머리 크기

        // 화살표 머리의 좌표 계산 (삼각형 모양을 더 세밀하게 구성)
        double[][] offsets = {
                {0, 0}, {-headSize * 0.5, -headSize * 0.25}, {-headSize * 0.5, headSize * 0.25},
                {-headSize, -headSize * 0.5}, {-headSize, headSize * 0.5}
        };

        for (double[] offset : offsets) {
            double x = offset[0];
            double z = offset[1];

            // 각 좌표를 목표 방향으로 회전
            double rotatedX = x * Math.cos(angle) - z * Math.sin(angle);
            double rotatedZ = x * Math.sin(angle) + z * Math.cos(angle);

            Location particleLocation = end.clone().add(rotatedX, 0, rotatedZ);
            player.spawnParticle(Particle.FLAME, particleLocation, 0, 0, 0, 0, 0);
        }
    }
}